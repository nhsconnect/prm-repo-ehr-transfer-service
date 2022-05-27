locals {
  account_id = data.aws_caller_identity.current.account_id
}

data "aws_iam_policy_document" "ecs-assume-role-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = [
        "ecs-tasks.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role" "component-ecs-role" {
  name               = "${var.environment}-${var.component_name}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs-assume-role-policy.json
  description        = "Role assumed by ${var.component_name} ECS task"

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}

data "aws_iam_policy_document" "ecr_policy_doc" {
  statement {
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:BatchGetImage"
    ]

    resources = [
      "arn:aws:ecr:${var.region}:${local.account_id}:repository/deductions/${var.component_name}"
    ]
  }
  statement {
    actions = [
      "ecr:GetAuthorizationToken"
    ]

    resources = [
      "*"
    ]
  }
}

data "aws_iam_policy_document" "logs_policy_doc" {
  statement {
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents"
    ]

    resources = [
      "arn:aws:logs:${var.region}:${local.account_id}:log-group:/nhs/deductions/${var.environment}-${local.account_id}/${var.component_name}:*"
    ]
  }
}

data "aws_iam_policy_document" "ssm_policy_doc" {
  statement {
    actions = [
      "ssm:Get*"
    ]

    resources = [
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/mq-app-username",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/mq-app-password",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/api-keys/gp-to-repo/ehr-transfer-service",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/api-keys/repo-to-gp/ehr-transfer-service",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/api-keys/ehr-repo/ehr-transfer-service",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/api-keys/gp2gp-messenger/ehr-transfer-service",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/output/prm-deductions-gp-to-repo/service-url",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/output/prm-deductions-repo-to-gp/repo-to-gp-service-url",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/output/prm-deductions-gp2gp-messenger/service-url",
      "arn:aws:ssm:${var.region}:${local.account_id}:parameter/repo/${var.environment}/user-input/external/repository-asid",
    ]
  }
}

resource "aws_iam_role_policy_attachment" "ehr_transfer_service_sqs" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.ehr_transfer_service_sqs.arn
}

resource "aws_iam_policy" "ehr_transfer_service_sqs" {
  name   = "${var.environment}-${var.component_name}-sqs"
  policy = data.aws_iam_policy_document.sqs_ehr_transfer_service_ecs_task.json
}

data "aws_iam_policy_document" "sqs_ehr_transfer_service_ecs_task" {
  statement {
    actions = [
      "sqs:GetQueue*",
      "sqs:ChangeMessageVisibility",
      "sqs:DeleteMessage",
      "sqs:ReceiveMessage"
    ]

    resources = [
      aws_sqs_queue.repo_incoming.arn,
      aws_sqs_queue.small_ehr.arn,
      aws_sqs_queue.ehr_complete.arn,
      aws_sqs_queue.transfer_complete.arn,
      aws_sqs_queue.large_ehr.arn
    ]
  }
}

resource "aws_iam_role_policy_attachment" "ehr_transfer_service_kms" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.ehr_transfer_service_kms.arn
}

resource "aws_iam_policy" "ehr_transfer_service_kms" {
  name   = "${var.environment}-${var.component_name}-kms"
  policy = data.aws_iam_policy_document.kms_policy_doc.json
}

data "aws_iam_policy_document" "kms_policy_doc" {
  statement {
    actions   = [
      "kms:*"
    ]
    resources = [
      "*"
    ]
  }
}

resource "aws_iam_policy" "gp2gp-ecr" {
  name   = "${var.environment}-ehr-transfer-service-ecr"
  policy = data.aws_iam_policy_document.ecr_policy_doc.json
}

resource "aws_iam_policy" "gp2gp-logs" {
  name   = "${var.environment}-ehr-transfer-service-logs"
  policy = data.aws_iam_policy_document.logs_policy_doc.json
}

resource "aws_iam_policy" "gp2gp-ssm" {
  name   = "${var.environment}-ehr-transfer-service-ssm"
  policy = data.aws_iam_policy_document.ssm_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "gp2gp-ecr-attach" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.gp2gp-ecr.arn
}

resource "aws_iam_role_policy_attachment" "gp2gp-ssm" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.gp2gp-ssm.arn
}

resource "aws_iam_role_policy_attachment" "gp2gp-logs" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.gp2gp-logs.arn
}

data "aws_iam_policy_document" "transfer-tracker-db-access" {
  statement {
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem",
      "dynamodb:UpdateItem"
    ]
    resources = [
      "arn:aws:dynamodb:${var.region}:${data.aws_caller_identity.current.account_id}:table/${aws_dynamodb_table.transfer_tracker.name}"
    ]
  }
}

resource "aws_iam_policy" "transfer-tracker-db-access" {
  name   = "${var.environment}-${var.component_name}-transfer-tracker-db-access"
  policy = data.aws_iam_policy_document.transfer-tracker-db-access.json
}

resource "aws_iam_role_policy_attachment" "ecs_dynamo_attach" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.transfer-tracker-db-access.arn
}

resource "aws_iam_role" "sns_failure_feedback_role" {
  name               = "${var.environment}-${var.component_name}-sns-failure-feedback-role"
  assume_role_policy = data.aws_iam_policy_document.sns_service_assume_role_policy.json
  description        = "Allows logging of SNS delivery failures in ${var.component_name}"

  tags = {
    Environment = var.environment
    CreatedBy   = var.repo_name
  }
}
resource "aws_iam_policy" "sns_failure_feedback_policy" {
  name   = "${var.environment}-${var.component_name}-sns-failure-feedback"
  policy = data.aws_iam_policy_document.sns_failure_feedback_policy.json
}

resource "aws_iam_role_policy_attachment" "sns_failure_feedback_policy_attachment" {
  role       = aws_iam_role.sns_failure_feedback_role.name
  policy_arn = aws_iam_policy.sns_failure_feedback_policy.arn
}

data "aws_iam_policy_document" "sns_failure_feedback_policy" {
  statement {
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:PutMetricFilter",
      "logs:PutRetentionPolicy"
    ]
    resources = [
      "*"
    ]
  }
}

data "aws_iam_policy_document" "sns_service_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "sns.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_policy" "sns" {
  name   = "${var.environment}-${var.component_name}-sns"
  policy = data.aws_iam_policy_document.sns_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "ehr_transfer_service_sns" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.sns.arn
}

data "aws_iam_policy_document" "sns_policy_doc" {
  statement {
    actions = [
      "sns:Publish",
      "sns:GetTopicAttributes"
    ]
    resources = [
      aws_sns_topic.parsing_dlq.arn,
      aws_sns_topic.positive_acks.arn,
      aws_sns_topic.attachments.arn,
      aws_sns_topic.large_ehr.arn,
      aws_sns_topic.small_ehr.arn,
      aws_sns_topic.negative_acks.arn,
      aws_sns_topic.ehr_complete.arn,
      aws_sns_topic.transfer_complete.arn
    ]
  }
}

resource "aws_sqs_queue_policy" "attachments" {
  queue_url = aws_sqs_queue.attachments.id
  policy    = data.aws_iam_policy_document.attachments_policy_doc.json
}

resource "aws_sqs_queue_policy" "attachments_observability" {
  queue_url = aws_sqs_queue.attachments_observability.id
  policy    = data.aws_iam_policy_document.attachments_policy_doc.json
}


resource "aws_sqs_queue_policy" "parsing_dlq" {
  queue_url = aws_sqs_queue.parsing_dlq.id
  policy    = data.aws_iam_policy_document.parsing_dlq_policy_doc.json
}

resource "aws_sqs_queue_policy" "positive_acks" {
  queue_url = aws_sqs_queue.positive_acks_observability.id
  policy    = data.aws_iam_policy_document.parsing_dlq_policy_doc.json
}

resource "aws_sqs_queue_policy" "large_ehr" {
  queue_url = aws_sqs_queue.large_ehr.id
  policy    = data.aws_iam_policy_document.large_ehr_policy_doc.json
}

resource "aws_sqs_queue_policy" "large_ehr_observability" {
  queue_url = aws_sqs_queue.large_ehr_observability.id
  policy    = data.aws_iam_policy_document.large_ehr_policy_doc.json
}

resource "aws_sqs_queue_policy" "small_ehr" {
  queue_url = aws_sqs_queue.small_ehr.id
  policy    = data.aws_iam_policy_document.small_ehr_policy_doc.json
}

resource "aws_sqs_queue_policy" "small_ehr_observability" {
  queue_url = aws_sqs_queue.small_ehr_observability.id
  policy    = data.aws_iam_policy_document.small_ehr_policy_doc.json
}

resource "aws_sqs_queue_policy" "negative_acks" {
  queue_url = aws_sqs_queue.negative_acks.id
  policy    = data.aws_iam_policy_document.negative_acks_policy_doc.json
}

resource "aws_sqs_queue_policy" "negative_acks_observability" {
  queue_url = aws_sqs_queue.negative_acks_observability.id
  policy    = data.aws_iam_policy_document.negative_acks_policy_doc.json
}

resource "aws_sqs_queue_policy" "ehr_complete" {
  queue_url = aws_sqs_queue.ehr_complete.id
  policy    = data.aws_iam_policy_document.ehr_complete_policy_doc.json
}

resource "aws_sqs_queue_policy" "ehr_complete_observability" {
  queue_url = aws_sqs_queue.ehr_complete_observability.id
  policy    = data.aws_iam_policy_document.ehr_complete_policy_doc.json
}

resource "aws_sqs_queue_policy" "transfer_complete" {
  queue_url = aws_sqs_queue.transfer_complete.id
  policy    = data.aws_iam_policy_document.transfer_complete_policy_doc.json
}

resource "aws_sqs_queue_policy" "transfer_complete_observability" {
  queue_url = aws_sqs_queue.transfer_complete_observability.id
  policy    = data.aws_iam_policy_document.transfer_complete_policy_doc.json
}

data "aws_iam_policy_document" "attachments_policy_doc" {
  statement {

    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.attachments.arn,
      aws_sqs_queue.attachments_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.attachments.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "parsing_dlq_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.parsing_dlq.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.parsing_dlq.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "positive_acks_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.positive_acks_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.positive_acks.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "large_ehr_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.large_ehr.arn,
      aws_sqs_queue.large_ehr_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.large_ehr.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "small_ehr_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.small_ehr.arn,
      aws_sqs_queue.small_ehr_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.small_ehr.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "ehr_complete_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.ehr_complete.arn,
      aws_sqs_queue.ehr_complete_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.ehr_complete.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "transfer_complete_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.transfer_complete.arn,
      aws_sqs_queue.transfer_complete_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.transfer_complete.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "negative_acks_policy_doc" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.negative_acks.arn,
      aws_sqs_queue.negative_acks_observability.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [aws_sns_topic.negative_acks.arn]
      variable = "aws:SourceArn"
    }
  }
}

data "aws_iam_policy_document" "sqs_large_message_bucket_access_policy_doc" {
  statement {
    sid = ""
    effect = "Allow"
    actions = [
      "s3:GetObject",
      "s3:PutObject",
    ]
    resources = [
      "arn:aws:s3:::${aws_s3_bucket.sqs_large_message_bucket.bucket}/*"
    ]
  }
}

resource "aws_iam_policy" "sqs_large_message_bucket_access_policy" {
  name   = "sqs_large_message_bucket"
  policy = data.aws_iam_policy_document.sqs_large_message_bucket_access_policy_doc.json
}

resource "aws_iam_role_policy_attachment" "ecs_role_s3_large_bucket_policy_attach" {
  role       = aws_iam_role.component-ecs-role.name
  policy_arn = aws_iam_policy.sqs_large_message_bucket_access_policy.arn
}

resource "aws_sqs_queue_policy" "repo_incoming" {
  queue_url = aws_sqs_queue.repo_incoming.id
  policy    = data.aws_iam_policy_document.repo_incoming_topic_access_to_queue.json
}

data "aws_iam_policy_document" "repo_incoming_topic_access_to_queue" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.repo_incoming.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [data.aws_ssm_parameter.repo_incoming_topic_arn.value]
      variable = "aws:SourceArn"
    }
  }
}

resource "aws_sqs_queue_policy" "repo_incoming_audit" {
  queue_url = aws_sqs_queue.repo_incoming_audit_queue.id
  policy    = data.aws_iam_policy_document.repo_incoming_audit_topic_access_to_queue.json
}

data "aws_iam_policy_document" "repo_incoming_audit_topic_access_to_queue" {
  statement {
    effect = "Allow"

    actions = [
      "sqs:SendMessage"
    ]

    principals {
      identifiers = ["sns.amazonaws.com"]
      type        = "Service"
    }

    resources = [
      aws_sqs_queue.repo_incoming_audit_queue.arn
    ]

    condition {
      test     = "ArnEquals"
      values   = [data.aws_ssm_parameter.repo_incoming_audit_sns_topic_arn.value]
      variable = "aws:SourceArn"
    }
  }
}