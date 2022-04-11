locals {
  repo_incoming_queue_name = "${var.environment}-${var.component_name}-repo-incoming"
  negative_acks_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments"
  negative_acks_observability_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments-observability"
  small_ehr_queue_name = "${var.environment}-${var.component_name}-small-ehr"
  small_ehr_observability_queue_name = "${var.environment}-${var.component_name}-small-ehr-observability"
  large_ehr_queue_name = "${var.environment}-${var.component_name}-large-ehr"
  large_ehr_observability_queue_name = "${var.environment}-${var.component_name}-large-ehr-observability"
  attachments_queue_name = "${var.environment}-${var.component_name}-attachments"
  attachments_observability_queue_name = "${var.environment}-${var.component_name}-attachments-observability"
  positive_acks_observability_queue_name = "${var.environment}-${var.component_name}-positive-acknowledgements-observability"
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

resource "aws_sqs_queue" "large_ehr" {
  name                       = local.large_ehr_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.large_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.large_ehr_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "large_ehr_observability" {
  name                       = local.large_ehr_observability_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.large_ehr.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.large_ehr_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "attachments" {
  name                       = local.attachments_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.attachments.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.attachments_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "attachments_observability" {
  name                       = local.attachments_observability_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.attachments.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.attachments_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "positive_acks_observability" {
  name                       = local.positive_acks_observability_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id          = aws_kms_key.positive_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.positive_acks_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}