locals {
  sns_topic_name = replace(var.sns_topic_arn, "/.*:/", "")
}

resource "aws_iam_policy" "this" {
  name        = "${local.sns_topic_name}-sns-policy"
  description = "IAM policy for SNS topic ${local.sns_topic_name} to enforce HTTPS"
  policy      = data.aws_iam_policy_document.enforce_https.json
}

data "aws_iam_policy_document" "enforce_https" {
  statement {
    effect = "Allow"
    actions = [
      "sns:GetTopicAttributes",
      "sns:SetTopicAttributes",
      "sns:AddPermission",
      "sns:RemovePermission",
      "sns:DeleteTopic",
      "sns:Subscribe",
      "sns:ListSubscriptionsByTopic"
    ]

    resources = [var.sns_topic_arn]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceOwner"
      values   = [var.account_id]
    }
  }

  statement {
    actions   = ["sns:Publish"]
    effect    = "Deny"
    resources = [var.sns_topic_arn]

    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }
}

resource "aws_sns_topic_policy" "this" {
  arn    = var.sns_topic_arn
  policy = aws_iam_policy.this.policy
}