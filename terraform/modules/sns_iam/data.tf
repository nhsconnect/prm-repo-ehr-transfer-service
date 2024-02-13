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
      values   = [data.aws_caller_identity.current]
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

data "aws_caller_identity" "current" {}