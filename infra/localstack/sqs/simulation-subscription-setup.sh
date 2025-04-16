#!/bin/bash
set -e

echo "🔗 Criando subscrição do tópico 'simulation-completed-topic' para a fila 'email-notification-queue'..."

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"
TOPIC_NAME="simulation-completed-topic"
QUEUE_NAME="email-notification-queue"

# Obter ARN do tópico
TOPIC_ARN=$(docker exec -i $CONTAINER_NAME awslocal sns list-topics \
  --region "$AWS_REGION" \
  --query "Topics[?contains(TopicArn, '$TOPIC_NAME')].TopicArn" \
  --output text)

# Obter URL e ARN da fila
QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-url \
  --queue-name "$QUEUE_NAME" \
  --region "$AWS_REGION" \
  --output text)

QUEUE_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attribute-name QueueArn \
  --output text --query 'Attributes.QueueArn')

# Criar subscrição
docker exec -i $CONTAINER_NAME awslocal sns subscribe \
  --topic-arn "$TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$QUEUE_ARN" \
  --region "$AWS_REGION"

echo "✅ Subscrição criada entre '$TOPIC_NAME' → '$QUEUE_NAME'"
