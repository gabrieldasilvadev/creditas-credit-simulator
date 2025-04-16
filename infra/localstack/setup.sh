#!/bin/bash
set -e
echo "🚀 Inicializando recursos no LocalStack..."

AWS_REGION="us-east-1"
TOPIC_NAME="credit-simulation-topic"
QUEUE_NAME="credit-simulation-queue"
CONTAINER_NAME="localstack"

TOPIC_ARN=$(docker exec -i $CONTAINER_NAME awslocal sns create-topic \
  --name "$TOPIC_NAME" \
  --region "$AWS_REGION" \
  --output text --query 'TopicArn')

QUEUE_URL=$(docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name "$QUEUE_NAME" \
  --attributes VisibilityTimeout=30,MessageRetentionPeriod=1200 \
  --region "$AWS_REGION" \
  --output text --query 'QueueUrl')

QUEUE_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attribute-name QueueArn \
  --output text --query 'Attributes.QueueArn')

ESCAPED_POLICY=$(bash "$(dirname "$0")/utils/render-policy.sh" "$QUEUE_ARN" "$TOPIC_ARN")

docker exec -i $CONTAINER_NAME awslocal sqs set-queue-attributes \
  --queue-url "$QUEUE_URL" \
  --attributes "{\"Policy\": \"$ESCAPED_POLICY\"}"

bash "$(dirname "$0")/sqs/email-queue-setup.sh"
bash "$(dirname "$0")/sns/simulation-topic-setup.sh"

echo "➡️ Criando subscrição do tópico de simulação para a fila de e-mail..."
bash "$(dirname "$0")/sqs/simulation-subscription-setup.sh"

echo "🏁 Setup finalizado com sucesso!"