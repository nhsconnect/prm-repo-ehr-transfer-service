locals {
  repo_incoming_queue_name = "${var.environment}-${var.component_name}-repo-incoming"
}

resource "aws_sqs_queue" "repo_incoming" {
  name                       = local.repo_incoming_queue_name
  message_retention_seconds  = 1209600
  kms_master_key_id = aws_kms_key.repo_incoming.id
  receive_wait_time_seconds = 20
  visibility_timeout_seconds = 240

  tags = {
    Name = local.repo_incoming_queue_name
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}