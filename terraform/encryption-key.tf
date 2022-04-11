resource "aws_kms_key" "repo_incoming" {
  description = "Custom KMS Key to enable server side encryption for SQS"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json

  tags = {
    Name        = "${var.environment}-${var.component_name}-repo-incoming-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "repo_incoming_encryption" {
  name          = "alias/repo-incoming-encryption-kms-key"
  target_key_id = aws_kms_key.repo_incoming.id
}

resource "aws_kms_key" "negative_acks" {
  description = "Custom KMS Key to enable server side encryption for negative acknowledgements SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json

  tags = {
    Name        = "${var.environment}-${var.component_name}-negative-acks-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "negative_acks_encryption" {
  name          = "alias/negative-acks-queue-encryption-kms-key"
  target_key_id = aws_kms_key.negative_acks.id
}

resource "aws_kms_key" "small_ehr" {
  description = "Custom KMS Key to enable server side encryption for small EHRs SQS queue"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json

  tags = {
    Name        = "${var.environment}-${var.component_name}-small-ehr-encryption-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_kms_alias" "small_ehr_encryption" {
  name          = "alias/small-ehr-queue-encryption-kms-key"
  target_key_id = aws_kms_key.small_ehr.id
}

resource "aws_kms_key" "transfer_tracker_dynamodb_kms_key" {
  description = "Custom KMS Key to enable server side encryption for Transfer Tracker DB"
  policy      = data.aws_iam_policy_document.kms_key_policy_doc.json

  tags = {
    Name        = "${var.environment}-${var.component_name}-transfer-tracker-dynamodb-kms-key"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

data "aws_iam_policy_document" "kms_key_policy_doc" {
  statement {
    effect = "Allow"

    principals {
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
      type        = "AWS"
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }

  statement {
    effect = "Allow"

    principals {
      identifiers = ["cloudwatch.amazonaws.com"]
      type        = "Service"
    }

    actions = [
      "kms:Decrypt",
      "kms:GenerateDataKey*"
    ]

    resources = ["*"]
  }
}