locals {
  repo_incoming_queue_name = "${var.environment}-${var.component_name}-repo-incoming"
  negative_acks_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments"
}

resource "aws_sqs_queue" "repo_incoming" {
  name                       = local.repo_incoming_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.repo_incoming.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.repo_incoming_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "negative_acks" {
  name                       = local.negative_acks_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.negative_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.negative_acks_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "negative_acks_topic" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = aws_sns_topic.negative_acks.arn
  endpoint             = aws_sqs_queue.negative_acks.arn
}