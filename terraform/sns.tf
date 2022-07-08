resource "aws_sns_topic" "negative_acks" {
  name = "${var.environment}-${var.component_name}-negative-acks-sns-topic"
  kms_master_key_id = aws_kms_key.negative_acks.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-negative-acks-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "small_ehr" {
  name = "${var.environment}-${var.component_name}-small-ehr-sns-topic"
  kms_master_key_id = aws_kms_key.small_ehr.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-small-ehr-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "large_ehr" {
  name = "${var.environment}-${var.component_name}-large-ehr-sns-topic"
  kms_master_key_id = aws_kms_key.large_ehr.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-large-ehr-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "large_message_fragments" {
  name = "${var.environment}-${var.component_name}-large-message-fragments-sns-topic"
  kms_master_key_id = aws_kms_key.large_message_fragments.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-large-message-fragments-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "positive_acks" {
  name = "${var.environment}-${var.component_name}-positive-acks-sns-topic"
  kms_master_key_id = aws_kms_key.positive_acks.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-positive-acks-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "parsing_dlq" {
  name = "${var.environment}-${var.component_name}-parsing-dlq-sns-topic"
  kms_master_key_id = aws_kms_key.parsing_dlq.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-parsing-dlq-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "ehr_complete" {
  name = "${var.environment}-${var.component_name}-ehr-complete-sns-topic"
  kms_master_key_id = aws_kms_key.ehr_complete.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-ehr-complete-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "transfer_complete" {
  name = "${var.environment}-${var.component_name}-transfer-complete-sns-topic"
  kms_master_key_id = aws_kms_key.transfer_complete.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-transfer-complete-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_sns_topic" "splunk_uploader" {
  name = "${var.environment}-${var.component_name}-splunk-uploader-sns-topic"
  kms_master_key_id = aws_kms_key.ehr_transfer_audit_kms_key.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-splunk-uploader-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}