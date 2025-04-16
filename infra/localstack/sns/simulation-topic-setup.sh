#!/bin/bash
set -e
echo "📣 Criando tópico simulation-completed-topic..."

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"
SIMULATION_TOPIC_NAME="simulation-completed-topic"

EXISTING_TOPIC=$(docker exec -i $CONTAINER_NAME awslocal sns list-topics --region "$AWS_REGION" --query "Topics[?contains(TopicArn, '$SIMULATION_TOPIC_NAME')].TopicArn" --output text)

if [ -z "$EXISTING_TOPIC" ]; then
  SIMULATION_TOPIC_ARN=$(docker exec -i $CONTAINER_NAME awslocal sns create-topic \
    --name "$SIMULATION_TOPIC_NAME" \
    --region "$AWS_REGION" \
    --output text --query 'TopicArn')
  echo "✅ Tópico '$SIMULATION_TOPIC_NAME' criado: $SIMULATION_TOPIC_ARN"
else
  echo "ℹ️ Tópico '$SIMULATION_TOPIC_NAME' já existe: $EXISTING_TOPIC"
fi