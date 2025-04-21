#!/bin/bash
set -euo pipefail

AWS_REGION="us-east-1"
AWS="aws --endpoint-url http://localstack:4566 --region $AWS_REGION"

echo "🚀 Iniciando setup completo no LocalStack..."

# 🔁 Função para criar tópico SNS se não existir
create_topic() {
  local topic_name="$1"
  echo "📣 Verificando existência do tópico: $topic_name"
  local topic_arn=$($AWS sns list-topics --query "Topics[?contains(TopicArn, '$topic_name')].TopicArn" --output text || true)

  if [[ -z "$topic_arn" ]]; then
    topic_arn=$($AWS sns create-topic --name "$topic_name" --output text --query 'TopicArn')
    echo "✅ Tópico '$topic_name' criado: $topic_arn"
  else
    echo "ℹ️ Tópico '$topic_name' já existe: $topic_arn"
  fi
}

# 🔁 Função para criar fila com RedrivePolicy e política de acesso
create_queue_with_policy() {
  local queue_name="$1"
  local topic_name="$2"

  echo "📥 Verificando existência da fila: $queue_name"
  local queue_url=$($AWS sqs get-queue-url --queue-name "$queue_name" --output text --query 'QueueUrl' 2>/dev/null || true)

  if [[ -z "$queue_url" ]]; then
    echo "📥 Criando fila: $queue_name"
    queue_url=$($AWS sqs create-queue \
      --queue-name "$queue_name" \
      --attributes VisibilityTimeout=30,MessageRetentionPeriod=1200 \
      --output text --query 'QueueUrl')
  else
    echo "ℹ️ Fila '$queue_name' já existe: $queue_url"
  fi

  local queue_arn=$($AWS sqs get-queue-attributes \
    --queue-url "$queue_url" \
    --attribute-name QueueArn \
    --output text --query 'Attributes.QueueArn')

  local topic_arn=$($AWS sns create-topic \
    --name "$topic_name" \
    --output text --query 'TopicArn')

  echo "🔐 Aplicando política de acesso à fila..."
  local raw_policy
  raw_policy=$(bash "$(dirname "$0")/utils/render-policy.sh" "$queue_arn" "$topic_arn")
  local escaped_policy
  escaped_policy=$(echo "$raw_policy" | jq -c . | sed 's/"/\\"/g')

  $AWS sqs set-queue-attributes \
    --queue-url "$queue_url" \
    --attributes "{\"Policy\": \"$escaped_policy\"}"

  echo "🔗 Subscrição do tópico na fila"
  $AWS sns subscribe \
    --topic-arn "$topic_arn" \
    --protocol sqs \
    --notification-endpoint "$queue_arn" || true \
    --attributes RawMessageDelivery=true
}

# ✅ Criando tópicos SNS
create_topic "simulation-completed-topic"
create_topic "credit-simulation-topic"

# ✅ Criando fila com política SNS → SQS
create_queue_with_policy "credit-simulation-queue" "credit-simulation-topic"

# 📤 DLQ para email
EMAIL_DLQ_URL=$($AWS sqs get-queue-url --queue-name email-notification-dlq --output text --query 'QueueUrl' 2>/dev/null || \
  $AWS sqs create-queue --queue-name email-notification-dlq --output text --query 'QueueUrl')

EMAIL_DLQ_ARN=$($AWS sqs get-queue-attributes \
  --queue-url "$EMAIL_DLQ_URL" \
  --attribute-name QueueArn \
  --output text --query 'Attributes.QueueArn')

# 📩 Fila principal com DLQ
$AWS sqs get-queue-url --queue-name email-notification-queue >/dev/null 2>&1 || \
$AWS sqs create-queue \
  --queue-name email-notification-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$EMAIL_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}\"}"

# 🪹 DLQ para bulk
BULK_DLQ_URL=$($AWS sqs get-queue-url --queue-name bulk-simulation-dlq --output text --query 'QueueUrl' 2>/dev/null || \
  $AWS sqs create-queue --queue-name bulk-simulation-dlq --output text --query 'QueueUrl')

BULK_DLQ_ARN=$($AWS sqs get-queue-attributes \
  --queue-url "$BULK_DLQ_URL" \
  --attribute-name QueueArn \
  --output text --query 'Attributes.QueueArn')

# 📦 Fila principal com DLQ
$AWS sqs get-queue-url --queue-name bulk-simulation-queue >/dev/null 2>&1 || \
$AWS sqs create-queue \
  --queue-name bulk-simulation-queue \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"$BULK_DLQ_ARN\\\",\\\"maxReceiveCount\\\":5}\"}"

echo "✅ Setup completo finalizado!"
