#!/bin/bash
set -e
echo "📨 Criando fila de e-mail com DLQ..."

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"
EMAIL_QUEUE_NAME="email-notification-queue"
EMAIL_DLQ_NAME="email-notification-dlq"

EMAIL_DLQ_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "$EMAIL_DLQ_NAME" \
  --region "$AWS_REGION" \
  --output text --query 'QueueUrl' || true)

EMAIL_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$EMAIL_DLQ_URL" \
  --attribute-name QueueArn \
  --output text --query 'Attributes.QueueArn')

EMAIL_QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "$EMAIL_QUEUE_NAME" \
  --attributes "{\"VisibilityTimeout\":\"30\",\"MessageRetentionPeriod\":\"1200\",\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$EMAIL_DLQ_ARN\\\",\\\"maxReceiveCount\\\":\\\"5\\\"}\"}" \
  --region "$AWS_REGION" \
  --output text --query 'QueueUrl')
echo "✅ Fila de e-mail criada com DLQ: $EMAIL_QUEUE_URL"