locals {
  ecs_cluster_id    = data.aws_ssm_parameter.deductions_private_ecs_cluster_id.value
  ecs_task_sg_id    = data.aws_ssm_parameter.deductions_private_gp2gp-message_handler_sg_id.value
  private_subnets   = split(",", data.aws_ssm_parameter.deductions_private_private_subnets.value)
}

resource "aws_ecs_service" "ecs-service" {
  name            = "${var.environment}-${var.component_name}-service"
  cluster         = local.ecs_cluster_id
  task_definition = aws_ecs_task_definition.task.arn
  desired_count   = var.service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups = [local.ecs_task_sg_id]
    subnets         = local.private_subnets
  }
}
