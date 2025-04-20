#!/bin/bash
set -e

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"
AWS_ACCOUNT_ID="000000000000"

echo "🚀 Iniciando setup completo no LocalStack..."

# 1. Criar tópicos SNS
echo "📣 Criando tópicos SNS..."
docker exec -i $CONTAINER_NAME awslocal sns create-topic --name "simulation-completed-topic" --region $AWS_REGION || true
docker exec -i $CONTAINER_NAME awslocal sns create-topic --name "credit-simulation-topic" --region $AWS_REGION || true

# 2. Criar filas SQS simples
echo "📥 Criando fila credit-simulation-queue..."
docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "credit-simulation-queue" \
  --attributes '{"VisibilityTimeout":"30","MessageRetentionPeriod":"1200"}' \
  --region $AWS_REGION || true

# 3. Criar filas DLQ
echo "📥 Criando filas DLQ..."
docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name "email-notification-dlq" --region $AWS_REGION || true
docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name "bulk-simulation-dlq" --region $AWS_REGION || true

# 4. Obter ARNs das DLQs
EMAIL_DLQ_URL="http://localhost:4566/000000000000/email-notification-dlq"
BULK_DLQ_URL="http://localhost:4566/000000000000/bulk-simulation-dlq"

EMAIL_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$EMAIL_DLQ_URL" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text \
  --region $AWS_REGION) || EMAIL_DLQ_ARN="arn:aws:sqs:$AWS_REGION:$AWS_ACCOUNT_ID:email-notification-dlq"

BULK_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$BULK_DLQ_URL" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text \
  --region $AWS_REGION) || BULK_DLQ_ARN="arn:aws:sqs:$AWS_REGION:$AWS_ACCOUNT_ID:bulk-simulation-dlq"

echo "📥 DLQ ARNs obtidos:"
echo "- Email DLQ: $EMAIL_DLQ_ARN"
echo "- Bulk DLQ: $BULK_DLQ_ARN"

# 5. Criar filas principais com DLQ
echo "📥 Criando filas com DLQ..."

# Cuidado extra com escape de JSON
EMAIL_REDRIVE_POLICY="{\\\"deadLetterTargetArn\\\":\\\"$EMAIL_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}"
BULK_REDRIVE_POLICY="{\\\"deadLetterTargetArn\\\":\\\"$BULK_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}"

docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "email-notification-queue" \
  --attributes "{\"RedrivePolicy\":\"$EMAIL_REDRIVE_POLICY\"}" \
  --region $AWS_REGION || true

docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "bulk-simulation-queue" \
  --attributes "{\"RedrivePolicy\":\"$BULK_REDRIVE_POLICY\"}" \
  --region $AWS_REGION || true

# 6. Tentar inscrições simplificadas
echo "🔗 Tentando inscrições simplificadas..."

# Definir ARNs para tópicos e filas
CREDIT_QUEUE_URL="http://localhost:4566/000000000000/credit-simulation-queue"
CREDIT_QUEUE_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$CREDIT_QUEUE_URL" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text \
  --region $AWS_REGION) || CREDIT_QUEUE_ARN="arn:aws:sqs:$AWS_REGION:$AWS_ACCOUNT_ID:credit-simulation-queue"

CREDIT_TOPIC_ARN="arn:aws:sns:$AWS_REGION:$AWS_ACCOUNT_ID:credit-simulation-topic"
SIMULATION_TOPIC_ARN="arn:aws:sns:$AWS_REGION:$AWS_ACCOUNT_ID:simulation-completed-topic"

echo "📥 ARNs para inscrições:"
echo "- Fila: $CREDIT_QUEUE_ARN"
echo "- Tópico Credit: $CREDIT_TOPIC_ARN"
echo "- Tópico Simulation: $SIMULATION_TOPIC_ARN"

# Política simplificada em uma linha única
POLICY_JSON='{\"Version\":\"2012-10-17\",\"Id\":\"AllowSNS\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"sqs:SendMessage\",\"Resource\":\"'$CREDIT_QUEUE_ARN'\",\"Condition\":{\"ArnEquals\":{\"aws:SourceArn\":[\"'$CREDIT_TOPIC_ARN'\",\"'$SIMULATION_TOPIC_ARN'\"]}}}]}'

echo "🔐 Aplicando política:"
echo "$POLICY_JSON"

# Aplicar política à fila
docker exec -i $CONTAINER_NAME awslocal sqs set-queue-attributes \
  --queue-url "$CREDIT_QUEUE_URL" \
  --attributes "{\"Policy\":\"$POLICY_JSON\"}" \
  --region $AWS_REGION || echo "⚠️ Erro ao aplicar política, mas continuando..."

# Tentar inscrição simplificada
echo "🔗 Tentando inscrição #1..."
docker exec -i $CONTAINER_NAME awslocal sns subscribe \
  --topic-arn "$CREDIT_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$CREDIT_QUEUE_ARN" \
  --region $AWS_REGION || echo "⚠️ Falha na inscrição #1, mas continuando..."

echo "🔗 Tentando inscrição #2..."
docker exec -i $CONTAINER_NAME awslocal sns subscribe \
  --topic-arn "$SIMULATION_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$CREDIT_QUEUE_ARN" \
  --region $AWS_REGION || echo "⚠️ Falha na inscrição #2, mas continuando..."

echo ""
echo "✅ Setup completo finalizado!"
echo "📣 Tópicos SNS: simulation-completed-topic, credit-simulation-topic"
echo "📥 Filas SQS: credit-simulation-queue, email-notification-queue (com DLQ), bulk-simulation-queue (com DLQ)"

# Verificar recursos
echo "🔍 Verificando tópicos SNS..."
docker exec -i $CONTAINER_NAME awslocal sns list-topics --region $AWS_REGION

echo "🔍 Verificando filas SQS..."
docker exec -i $CONTAINER_NAME awslocal sqs list-queues --region $AWS_REGION

echo "🔍 Tentando verificar subscrições (pode falhar)..."
docker exec -i $CONTAINER_NAME awslocal sns list-subscriptions --region $AWS_REGION || echo "⚠️ Não foi possível listar subscrições"
