#!/bin/bash
set -e

QUEUE_ARN=$1
TOPIC_ARN=$2

cat "$(dirname "$0")/../config/policy.json" \
  | sed "s|REPLACE_WITH_QUEUE_ARN|$QUEUE_ARN|g" \
  | sed "s|REPLACE_WITH_TOPIC_ARN|$TOPIC_ARN|g" \
  | tr -d '\n\r' | sed 's/"/\\"/g'
