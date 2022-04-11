resource "aws_sns_topic_subscription" "negative_acks_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.negative_acks.arn
  endpoint             = aws_sqs_queue.negative_acks.arn
}

resource "aws_sns_topic_subscription" "negative_acks_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.negative_acks.arn
  endpoint             = aws_sqs_queue.negative_acks_observability.arn
}

resource "aws_sns_topic_subscription" "small_ehr_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.small_ehr.arn
  endpoint             = aws_sqs_queue.small_ehr.arn
}

resource "aws_sns_topic_subscription" "small_ehr_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.small_ehr.arn
  endpoint             = aws_sqs_queue.small_ehr_observability.arn
}

resource "aws_sns_topic_subscription" "large_ehr_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.large_ehr.arn
  endpoint             = aws_sqs_queue.large_ehr.arn
}

resource "aws_sns_topic_subscription" "large_ehr_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.large_ehr.arn
  endpoint             = aws_sqs_queue.large_ehr_observability.arn
}

resource "aws_sns_topic_subscription" "attachments_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.attachments.arn
  endpoint             = aws_sqs_queue.attachments.arn
}

resource "aws_sns_topic_subscription" "attachments_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.attachments.arn
  endpoint             = aws_sqs_queue.attachments_observability.arn
}

resource "aws_sns_topic_subscription" "positive_acks_observability_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.positive_acks.arn
  endpoint             = aws_sqs_queue.positive_acks_observability.arn
}

resource "aws_sns_topic_subscription" "parsing_dlq_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.parsing_dlq.arn
  endpoint             = aws_sqs_queue.parsing_dlq.arn
}