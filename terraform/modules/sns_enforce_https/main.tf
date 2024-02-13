locals {
  sns_topic_name = replace(var.sns_topic_arn, "/.*:/", "")
}

resource "aws_iam_policy" "this" {
  name        = "${local.sns_topic_name}-sns-policy"
  description = "IAM policy for SNS topic ${local.sns_topic_name} to enforce HTTPS"
  policy      = data.aws_iam_policy_document.enforce_https.json
}

resource "aws_sns_topic_policy" "this" {
  arn    = var.sns_topic_arn
  policy = aws_iam_policy.this.policy
}