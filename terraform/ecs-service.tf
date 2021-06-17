locals {
  ecs_cluster_id    = data.aws_ssm_parameter.deductions_private_ecs_cluster_id.value
  ecs_task_sg_id    = data.aws_ssm_parameter.deductions_private_gp2gp_message_handler_sg_id.value
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

data "aws_ssm_parameter" "service-to-ehr-repo-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-ehr-repository/service-to-ehr-repo-sg-id"
}

resource "aws_security_group_rule" "gp2gp-message-handler-to-ehr-repo" {
  type = "ingress"
  protocol = "TCP"
  from_port = 443
  to_port = 443
  security_group_id = data.aws_ssm_parameter.service-to-ehr-repo-sg-id.value
  source_security_group_id = data.aws_ssm_parameter.deductions_private_gp2gp_message_handler_sg_id.value
}

data "aws_ssm_parameter" "service-to-gp-to-repo-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-gp-to-repo/service-to-gp-to-repo-sg-id"
}

resource "aws_security_group_rule" "gp2gp-message-handler-to-gp-to-repo" {
  type = "ingress"
  protocol = "TCP"
  from_port = 443
  to_port = 443
  security_group_id = data.aws_ssm_parameter.service-to-gp-to-repo-sg-id.value
  source_security_group_id = data.aws_ssm_parameter.deductions_private_gp2gp_message_handler_sg_id.value
}


