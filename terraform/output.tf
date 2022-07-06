resource "aws_ssm_parameter" "transfer_complete_kms_key" {
  name  = "/repo/${var.environment}/output/${var.component_name}/transfer-complete-encryption-kms-key"
  type  = "String"
  value = aws_kms_key.transfer_complete.id

  tags = {
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}