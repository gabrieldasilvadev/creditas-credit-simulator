#!/bin/bash
set -e

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"
AWS_ACCOUNT_ID="000000000000"

echo "🚀 Iniciando setup completo no LocalStack..."

# Criar tópicos
docker exec -i $CONTAINER_NAME awslocal sns create-topic --name simulation-completed-topic
docker exec -i $CONTAINER_NAME awslocal sns create-topic --name credit-simulation-topic

# Criar DLQs
docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name email-notification-dlq
docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name bulk-simulation-dlq

# Obter ARNs das DLQs
EMAIL_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/email-notification-dlq \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

BULK_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/bulk-simulation-dlq \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

# Criar filas principais com DLQ (JSON devidamente escapado)
docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name email-notification-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$EMAIL_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}\"}"

docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name bulk-simulation-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$BULK_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}\"}"

# Obter URL e ARN da fila email-notification-queue
EMAIL_QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-url \
  --queue-name email-notification-queue \
  --query 'QueueUrl' --output text)

EMAIL_QUEUE_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$EMAIL_QUEUE_URL" \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

# Definir ARN do tópico
SIMULATION_TOPIC_ARN="arn:aws:sns:$AWS_REGION:$AWS_ACCOUNT_ID:simulation-completed-topic"

# Construir a policy com escape duplo de aspas
ESCAPED_POLICY="{\\\"Version\\\":\\\"2012-10-17\\\",\\\"Statement\\\":[{\\\"Effect\\\":\\\"Allow\\\",\\\"Principal\\\":\\\"*\\\",\\\"Action\\\":\\\"sqs:SendMessage\\\",\\\"Resource\\\":\\\"$EMAIL_QUEUE_ARN\\\",\\\"Condition\\\":{\\\"ArnEquals\\\":{\\\"aws:SourceArn\\\":\\\"$SIMULATION_TOPIC_ARN\\\"}}}]}"

# Aplicar policy
docker exec -i $CONTAINER_NAME awslocal sqs set-queue-attributes \
  --queue-url "$EMAIL_QUEUE_URL" \
  --attributes "{\"Policy\":\"$ESCAPED_POLICY\"}"

# Criar subscrição do tópico para a fila
docker exec -i $CONTAINER_NAME awslocal sns subscribe \
  --topic-arn "$SIMULATION_TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$EMAIL_QUEUE_ARN"

echo "✅ Subscrição concluída com sucesso!"
