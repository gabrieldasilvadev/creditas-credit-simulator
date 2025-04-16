#!/bin/bash
set -e

echo "🧹 Removendo recursos do LocalStack (via docker exec)..."

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"

### SNS Topic

TOPIC_NAME="credit-simulation-topic"
TOPIC_ARN=$(docker exec -i $CONTAINER_NAME awslocal sns list-topics \
  --region "$AWS_REGION" \
  --query "Topics[?contains(TopicArn, '$TOPIC_NAME')].TopicArn" \
  --output text)

if [ -z "$TOPIC_ARN" ]; then
  echo "⚠️ Tópico '$TOPIC_NAME' não encontrado."
else
  echo "🗑️ Deletando tópico SNS: $TOPIC_ARN"
  docker exec -i $CONTAINER_NAME awslocal sns delete-topic \
    --topic-arn "$TOPIC_ARN" --region "$AWS_REGION"
fi

### Fila credit-simulation-queue

SIMULATION_QUEUE_NAME="credit-simulation-queue"
SIMULATION_QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-url \
  --queue-name "$SIMULATION_QUEUE_NAME" \
  --region "$AWS_REGION" \
  --output text 2>/dev/null || echo "")

if [ -z "$SIMULATION_QUEUE_URL" ]; then
  echo "⚠️ Fila '$SIMULATION_QUEUE_NAME' não encontrada."
else
  echo "🗑️ Deletando fila SQS: $SIMULATION_QUEUE_URL"
  docker exec -i $CONTAINER_NAME awslocal sqs delete-queue \
    --queue-url "$SIMULATION_QUEUE_URL" --region "$AWS_REGION"
fi

### Fila de e-mail

EMAIL_QUEUE_NAME="email-notification-queue"
EMAIL_QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-url \
  --queue-name "$EMAIL_QUEUE_NAME" \
  --region "$AWS_REGION" \
  --output text 2>/dev/null || echo "")

if [ -z "$EMAIL_QUEUE_URL" ]; then
  echo "⚠️ Fila '$EMAIL_QUEUE_NAME' não encontrada."
else
  echo "🗑️ Deletando fila de e-mail: $EMAIL_QUEUE_URL"
  docker exec -i $CONTAINER_NAME awslocal sqs delete-queue \
    --queue-url "$EMAIL_QUEUE_URL" --region "$AWS_REGION"
fi

### DLQ da fila de e-mail

EMAIL_DLQ_NAME="email-notification-dlq"
EMAIL_DLQ_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-url \
  --queue-name "$EMAIL_DLQ_NAME" \
  --region "$AWS_REGION" \
  --output text 2>/dev/null || echo "")

if [ -z "$EMAIL_DLQ_URL" ]; then
  echo "⚠️ DLQ '$EMAIL_DLQ_NAME' não encontrada."
else
  echo "🗑️ Deletando DLQ: $EMAIL_DLQ_URL"
  docker exec -i $CONTAINER_NAME awslocal sqs delete-queue \
    --queue-url "$EMAIL_DLQ_URL" --region "$AWS_REGION"
fi

echo "✅ Todos os recursos foram removidos com sucesso!"
