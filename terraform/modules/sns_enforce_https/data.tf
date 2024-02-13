data "aws_iam_policy_document" "enforce_https" {
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