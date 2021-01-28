locals {
    task_role_arn                = aws_iam_role.gp2gp.arn
    task_execution_role          = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/${var.environment}-${var.component_name}-EcsTaskRole"
    task_ecr_url                 = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com"
    task_log_group               = "/nhs/deductions/${var.environment}-${data.aws_caller_identity.current.account_id}/${var.component_name}"
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
        image_name            = "deductions/gp2gp-message-handler",
        image_tag             = var.task_image_tag,
        cpu                   = var.task_cpu,
        memory                = var.task_memory,
        log_region            = var.region,
        log_group             = local.task_log_group
    })

  tags = {
    Environment = var.environment
    CreatedBy = var.repo_name
  }
}
