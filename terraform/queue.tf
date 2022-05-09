locals {
  repo_incoming_queue_name = "${var.environment}-${var.component_name}-repo-incoming"
  repo_incoming_observability_queue_name = "${var.environment}-${var.component_name}-repo-incoming-observability"
  repo_incoming_audit_queue_name = "${var.environment}-${var.component_name}-repo-incoming-audit"
  negative_acks_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments"
  negative_acks_observability_queue_name = "${var.environment}-${var.component_name}-negative-acknowledgments-observability"
  small_ehr_queue_name = "${var.environment}-${var.component_name}-small-ehr"
  small_ehr_observability_queue_name = "${var.environment}-${var.component_name}-small-ehr-observability"
  large_ehr_queue_name = "${var.environment}-${var.component_name}-large-ehr"
  large_ehr_observability_queue_name = "${var.environment}-${var.component_name}-large-ehr-observability"
  attachments_queue_name = "${var.environment}-${var.component_name}-attachments"
  attachments_observability_queue_name = "${var.environment}-${var.component_name}-attachments-observability"
  positive_acks_observability_queue_name = "${var.environment}-${var.component_name}-positive-acknowledgements-observability"
  parsing_dlq_name = "${var.environment}-${var.component_name}-parsing-dlq"
  max_retention_period = 1209600
  thirty_minute_retention_period = 1800
}

resource "aws_sqs_queue" "repo_incoming" {
  name                       = local.repo_incoming_queue_name
  message_retention_seconds  = local.max_retention_period
  kms_master_key_id          = data.aws_ssm_parameter.repo_incoming_kms_key.value
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
  message_retention_seconds  = local.max_retention_period
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
  message_retention_seconds  = local.thirty_minute_retention_period
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
  message_retention_seconds  = local.max_retention_period
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
  message_retention_seconds  = local.thirty_minute_retention_period
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
  message_retention_seconds  = local.max_retention_period
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
  message_retention_seconds  = local.thirty_minute_retention_period
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
  message_retention_seconds  = local.max_retention_period
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
  message_retention_seconds  = local.thirty_minute_retention_period
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
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.positive_acks.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.positive_acks_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "parsing_dlq" {
  name                       = local.parsing_dlq_name
  message_retention_seconds  = local.thirty_minute_retention_period
  kms_master_key_id          = aws_kms_key.parsing_dlq.id
  receive_wait_time_seconds  = 20
  visibility_timeout_seconds = 240

  tags = {
    Name        = local.parsing_dlq_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sqs_queue" "repo_incoming_observability_queue" {
  name                       = local.repo_incoming_observability_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id = data.aws_ssm_parameter.repo_incoming_observability_kms_key.value

  tags = {
    Name = local.repo_incoming_observability_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "repo_incoming_observability_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = data.aws_ssm_parameter.repo_incoming_observability_sns_topic_arn.value
  endpoint             = aws_sqs_queue.repo_incoming_observability_queue.arn
}

resource "aws_sqs_queue" "repo_incoming_audit_queue" {
  name                       = local.repo_incoming_audit_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id = data.aws_ssm_parameter.repo_incoming_audit_kms_key.value

  tags = {
    Name = local.repo_incoming_audit_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic_subscription" "repo_incoming_audit_queue" {
  protocol             = "sqs"
  raw_message_delivery = true
  topic_arn            = data.aws_ssm_parameter.repo_incoming_audit_sns_topic_arn.value
  endpoint             = aws_sqs_queue.repo_incoming_audit_queue.arn
}
