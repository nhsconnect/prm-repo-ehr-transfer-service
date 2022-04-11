locals {
  repo_incoming_queue_name = "${var.environment}-${var.component_name}-repo-incoming"
  negative_acks_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments"
  negative_acks_observability_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments-observability"
  small_ehr_queue_name = "${var.environment}-${var.component_name}-small-ehr"
  small_ehr_observability_queue_name = "${var.environment}-${var.component_name}-small-ehr-observability"
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

resource "aws_sqs_queue" "negative_acks_observability" {
  name                       = local.negative_acks_observability_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.negative_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.negative_acks_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "small_ehr" {
  name                       = local.small_ehr_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.small_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.small_ehr_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "small_ehr_observability" {
  name                       = local.small_ehr_observability_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.small_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.small_ehr_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}