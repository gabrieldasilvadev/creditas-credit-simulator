#!/bin/bash
set -e

echo "🧹 Removendo todos os recursos do LocalStack..."

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"

# Função para deletar fila SQS
delete_queue() {
  local queue_name="$1"
  echo "🔍 Verificando fila: $queue_name"
  local queue_url=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-url \
    --queue-name "$queue_name" --region "$AWS_REGION" --output text 2>/dev/null || echo "")

  if [ -z "$queue_url" ]; then
    echo "⚠️ Fila '$queue_name' não encontrada."
  else
    echo "🗑️ Deletando fila: $queue_url"
    docker exec -i $CONTAINER_NAME awslocal sqs delete-queue \
      --queue-url "$queue_url" --region "$AWS_REGION"
  fi
}

# Função para deletar tópico SNS
delete_topic() {
  local topic_name="$1"
  echo "🔍 Verificando tópico: $topic_name"
  local topic_arn=$(docker exec -i $CONTAINER_NAME awslocal sns list-topics --region "$AWS_REGION" \
    --query "Topics[?contains(TopicArn, '$topic_name')].TopicArn" --output text)

  if [ -z "$topic_arn" ]; then
    echo "⚠️ Tópico '$topic_name' não encontrado."
  else
    echo "🗑️ Deletando tópico SNS: $topic_arn"
    docker exec -i $CONTAINER_NAME awslocal sns delete-topic \
      --topic-arn "$topic_arn" --region "$AWS_REGION"
  fi
}

# Deletar tópicos
delete_topic "credit-simulation-topic"
delete_topic "simulation-completed-topic"

# Deletar filas
delete_queue "credit-simulation-queue"
delete_queue "email-notification-queue"
delete_queue "email-notification-dlq"
delete_queue "bulk-simulation-queue"
delete_queue "bulk-simulation-dlq"

echo "✅ Todos os recursos foram removidos com sucesso!"
