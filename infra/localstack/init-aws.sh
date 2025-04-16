#!/bin/bash
set -e

echo "🚀 Inicializando recursos no LocalStack (via docker exec)..."

AWS_REGION="us-east-1"
TOPIC_NAME="credit-simulation-topic"
QUEUE_NAME="credit-simulation-queue"
CONTAINER_NAME="localstack"

# Criar tópico SNS
TOPIC_ARN=$(docker exec -i $CONTAINER_NAME awslocal sns create-topic \
  --name "$TOPIC_NAME" \
  --region "$AWS_REGION" \
  --output text --query 'TopicArn')
echo "✅ Tópico criado: $TOPIC_ARN"

# Criar fila SQS
QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "$QUEUE_NAME" \
  --attributes VisibilityTimeout=30,MessageRetentionPeriod=1200 \
  --region "$AWS_REGION" \
  --output text --query 'QueueUrl')
echo "✅ Fila criada: $QUEUE_URL"

# Obter ARN da fila
QUEUE_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attribute-name QueueArn \
  --output text --query 'Attributes.QueueArn')
echo "📥 Fila ARN: $QUEUE_ARN"

# Criar subscrição SNS → SQS
docker exec -i $CONTAINER_NAME awslocal sns subscribe \
  --topic-arn "$TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" \
  --region "$AWS_REGION"
echo "🔗 Subscrição criada entre SNS e SQS"

# Aplicar política com placeholders substituídos e minificada
POLICY=$(cat "$(dirname "$0")/config/policy.json" \
  | sed "s|REPLACE_WITH_QUEUE_ARN|$QUEUE_ARN|g" \
  | sed "s|REPLACE_WITH_TOPIC_ARN|$TOPIC_ARN|g" \
  | tr -d '\n\r' | tr -s ' ')

docker exec -i $CONTAINER_NAME awslocal sqs set-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attributes "{\"Policy\": \"$POLICY\"}"
echo "✅ Política aplicada com sucesso"

echo "➡️ Executando setup da fila de e-mail com DLQ (dentro do container)..."
bash /etc/localstack/init/ready.d/email-queue-setup.sh

echo "➡️ Executando setup do tópico simulation-completed-topic..."
bash /etc/localstack/init/ready.d/simulation-topic-setup.sh

echo "➡️ Executando subscrição do tópico para a fila de e-mail..."
bash /etc/localstack/init/ready.d/simulation-subscription-setup.sh

echo "🏁 Setup finalizado com sucesso!"
