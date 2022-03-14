locals {
    task_role_arn                = aws_iam_role.gp2gp.arn
    task_execution_role          = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.environment}-${var.component_name}-EcsTaskRole"
    task_ecr_url                 = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
    task_log_group               = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"
    environment_variables        = [
      { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_URL_1", value = data.aws_ssm_parameter.openwire_endpoint_0.value },
      { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_URL_2", value = data.aws_ssm_parameter.openwire_endpoint_1.value },
      { name = "EHR_TRANSFER_SERVICE_GP_TO_REPO_URL", value = "https://gp-to-repo.${var.environment}.non-prod.patient-deductions.nhs.uk" },
      { name = "EHR_TRANSFER_SERVICE_REPO_TO_GP_URL", value = "https://repo-to-gp.${var.environment}.non-prod.patient-deductions.nhs.uk" },
      { name = "EHR_TRANSFER_SERVICE_EHR_REPO_URL", value = "https://ehr-repo.${var.environment}.non-prod.patient-deductions.nhs.uk" },
      { name = "EHR_TRANSFER_SERVICE_LOG_LEVEL", value = var.log_level },
      { name = "EHR_TRANSFER_SERVICE_LOG_LEVEL", value = var.log_level },
      { name = "NHS_ENVIRONMENT", value = var.environment }

    ]
    secret_environment_variables = [
      { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_USERNAME", valueFrom = data.aws_ssm_parameter.amq-username.arn },
      { name = "EHR_TRANSFER_SERVICE_MHS_QUEUE_PASSWORD", valueFrom = data.aws_ssm_parameter.amq-password.arn },
      { name = "EHR_TRANSFER_SERVICE_AUTHORIZATION_KEYS_FOR_GP_TO_REPO", valueFrom = data.aws_ssm_parameter.ehr_transfer_service_authorization_keys_for_gp_to_repo.arn },
      { name = "EHR_TRANSFER_SERVICE_AUTHORIZATION_KEYS_FOR_REPO_TO_GP", valueFrom = data.aws_ssm_parameter.ehr_transfer_service_authorization_keys_for_repo_to_gp.arn },
      { name = "EHR_TRANSFER_SERVICE_AUTHORIZATION_KEYS_FOR_EHR_REPO", valueFrom = data.aws_ssm_parameter.ehr_transfer_service_authorization_keys_for_ehr_repo.arn },
    ]
}

resource "aws_ecs_task_definition" "task" {
  family                    = var.component_name
  network_mode              = "awsvpc"
  requires_compatibilities  = ["FARGATE"]
  cpu                       = var.task_cpu
  memory                    = var.task_memory
  execution_role_arn        = local.task_execution_role
  task_role_arn             = local.task_role_arn

  container_definitions  =  templatefile("${path.module}/templates/ecs-task-def.tmpl", {
        container_name        = "${var.component_name}-container",
        ecr_url               = local.task_ecr_url,
        image_name            = "deductions/ehr-transfer-service",
        image_tag             = var.task_image_tag,
        cpu                   = var.task_cpu,
        memory                = var.task_memory,
        log_region            = var.region,
        log_group             = local.task_log_group
        environment_variables = jsonencode(local.environment_variables),
        secrets               = jsonencode(local.secret_environment_variables)
    })

  tags = {
    Environment = var.environment
    CreatedBy = var.repo_name
  }
}
