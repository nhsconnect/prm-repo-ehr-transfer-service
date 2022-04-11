locals {
  small_ehr_queue_s3 = "${var.environment}-${var.component_name}-small-ehr-queue-s3"
}
resource "aws_s3_bucket" "small-ehr-queue-bucket" {
  bucket        = local.small_ehr_queue_s3
  acl           = "private"
  force_destroy = true

  versioning {
    enabled = false
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

resource "aws_s3_bucket_policy" "small_ehr_queue_bucket_policy" {

  bucket = aws_s3_bucket.small-ehr-queue-bucket.id
  policy = jsonencode({
    "Statement": [
      {
        Effect: "Deny",
        Principal: "*",
        Action: "s3:*",
        Resource: "${aws_s3_bucket.small-ehr-queue-bucket.arn}/*",
        Condition: {
          Bool: {
            "aws:SecureTransport": "false"
          }
        }
      }
    ]
  })
}