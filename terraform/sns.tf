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

resource "aws_sns_topic" "attachments" {
  name = "${var.environment}-${var.component_name}-attachments-sns-topic"
  kms_master_key_id = aws_kms_key.attachments.id
  sqs_failure_feedback_role_arn = aws_iam_role.sns_failure_feedback_role.arn

  tags = {
    Name = "${var.environment}-${var.component_name}-attachments-sns-topic"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}