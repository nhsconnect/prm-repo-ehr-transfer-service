data "aws_caller_identity" "current" {}

data "aws_ssm_parameter" "deductions_private_private_subnets" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-private-private-subnets"
}

data "aws_ssm_parameter" "deductions_private_vpc_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/private-vpc-id"
}

data "aws_ssm_parameter" "deductions_core_vpc_id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/deductions-core-vpc-id"
}

data "aws_ssm_parameter" "amq-username" {
  name = "/repo/${var.environment}/user-input/mq-app-username"
}

data "aws_ssm_parameter" "amq-password" {
  name = "/repo/${var.environment}/user-input/mq-app-password"
}

data "aws_ssm_parameter" "openwire_endpoint_0" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/openwire-endpoint-0"
}

data "aws_ssm_parameter" "openwire_endpoint_1" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/openwire-endpoint-1"
}

data "aws_ssm_parameter" "ehr_transfer_service_authorization_keys_for_gp_to_repo" {
  name = "/repo/${var.environment}/user-input/api-keys/gp-to-repo/ehr-transfer-service"
}

data "aws_ssm_parameter" "ehr_transfer_service_authorization_keys_for_repo_to_gp" {
  name = "/repo/${var.environment}/user-input/api-keys/repo-to-gp/ehr-transfer-service"
}

data "aws_ssm_parameter" "ehr_transfer_service_authorization_keys_for_ehr_repo" {
  name = "/repo/${var.environment}/user-input/api-keys/ehr-repo/ehr-transfer-service"
}