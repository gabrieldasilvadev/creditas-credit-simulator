#!/bin/bash
set -e

AWS_REGION="us-east-1"
CONTAINER_NAME="localstack"

echo "🚀 Iniciando setup completo no LocalStack..."

# Função para criar tópico SNS se não existir
create_topic() {
  local topic_name="$1"
  echo "📣 Verificando existência do tópico: $topic_name"
  local existing_topic=$(docker exec -i $CONTAINER_NAME awslocal sns list-topics --region "$AWS_REGION" --query "Topics[?contains(TopicArn, '$topic_name')].TopicArn" --output text)

  if [ -z "$existing_topic" ]; then
    local topic_arn=$(docker exec -i $CONTAINER_NAME awslocal sns create-topic --name "$topic_name" --region "$AWS_REGION" --output text --query 'TopicArn')
    echo "✅ Tópico '$topic_name' criado: $topic_arn"
  else
    echo "ℹ️ Tópico '$topic_name' já existe: $existing_topic"
  fi
}

# Função para criar fila SQS com atributos e política
create_queue_with_policy() {
  local queue_name="$1"
  local topic_name="$2"

  echo "📥 Criando fila: $queue_name"
  local queue_url=$(docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name "$queue_name" --attributes VisibilityTimeout=30,MessageRetentionPeriod=1200 --region "$AWS_REGION" --output text --query 'QueueUrl')

  local queue_arn=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes --queue-url "$queue_url" --attribute-name QueueArn --output text --query 'Attributes.QueueArn')
  local topic_arn=$(docker exec -i $CONTAINER_NAME awslocal sns create-topic --name "$topic_name" --region "$AWS_REGION" --output text --query 'TopicArn')

  echo "🔐 Aplicando política de acesso à fila..."
  local policy=$(bash "$(dirname "$0")/utils/render-policy.sh" "$queue_arn" "$topic_arn")
  docker exec -i $CONTAINER_NAME awslocal sqs set-queue-attributes --queue-url "$queue_url" --attributes "{\"Policy\": \"$policy\"}"

  echo "🔗 Subscrição do tópico na fila"
  docker exec -i $CONTAINER_NAME awslocal sns subscribe --topic-arn "$topic_arn" --protocol sqs --notification-endpoint "$queue_arn" --region "$AWS_REGION"
}

# Criar tópicos
create_topic "simulation-completed-topic"
create_topic "credit-simulation-topic"

# Criar fila com política SNS → SQS
create_queue_with_policy "credit-simulation-queue" "credit-simulation-topic"

# DLQ da fila de e-mail
docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name email-notification-dlq
EMAIL_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/email-notification-dlq --attribute-name QueueArn --query 'Attributes.QueueArn' --output text)

# Fila principal com RedrivePolicy para e-mail
docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name email-notification-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$EMAIL_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}\"}"

# DLQ da fila bulk
docker exec -i $CONTAINER_NAME awslocal sqs create-queue --queue-name bulk-simulation-dlq
BULK_DLQ_ARN=$(docker exec -i $CONTAINER_NAME awslocal sqs get-queue-attributes --queue-url http://localhost:4566/000000000000/bulk-simulation-dlq --attribute-name QueueArn --query 'Attributes.QueueArn' --output text)

# Fila principal com RedrivePolicy para bulk
docker exec -i $CONTAINER_NAME awslocal sqs create-queue \
  --queue-name bulk-simulation-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$BULK_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}\"}"

echo "✅ Setup completo finalizado!"
