locals {
  sqs_large_message_bucket_name = "${var.environment}-${var.component_name}-sqs-large-messages"
}
resource "aws_s3_bucket" "sqs_large_message_bucket" {
  bucket        = local.sqs_large_message_bucket_name
  acl           = "private"
  force_destroy = true

  versioning {
    enabled = false
  }

  logging {
    target_bucket = data.aws_s3_bucket.log_bucket.id
    target_prefix = "sqs-large-message-access-log/"
  }

  server_side_encryption_configuration {
    rule {
      apply_server_side_encryption_by_default {
        sse_algorithm = "AES256"
      }
    }
  }

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}

resource "aws_s3_bucket_policy" "ehr-repo-sqs_large_message_bucket_policy" {
  bucket = aws_s3_bucket.sqs_large_message_bucket.id
  policy = jsonencode({
    "Version": "2008-10-17"
    "Statement": [
      {
        Effect: "Deny",
        Principal: "*",
        Action: "s3:*",
        Resource: "${aws_s3_bucket.sqs_large_message_bucket.arn}/*",
        Condition: {
          Bool: {
            "aws:SecureTransport": "false"
          }
        }
      },
      {
        Effect: "Deny",
        Principal:  {
          "AWS": "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/NHSDAdminRole"
        },
        Action: "s3:*",
        Resource: [
          "${aws_s3_bucket.sqs_large_message_bucket.arn}",
          "${aws_s3_bucket.sqs_large_message_bucket.arn}/*"
        ]
      },
    ]
  })
}

data "aws_s3_bucket" "log_bucket" {
  bucket = "${var.environment}-ehr-repo-log-bucket"
}