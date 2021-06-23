locals {
  private_subnets   = split(",", data.aws_ssm_parameter.deductions_private_private_subnets.value)
}

resource "aws_ecs_service" "ecs-service" {
  name            = "${var.environment}-${var.component_name}-service"
  cluster         = aws_ecs_cluster.gp2gp_message_handler_ecs_cluster.id
  task_definition = aws_ecs_task_definition.task.arn
  desired_count   = var.service_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups = [aws_security_group.gp2gp-message-handler-ecs-task-sg.id]
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
  source_security_group_id = aws_security_group.gp2gp-message-handler-ecs-task-sg.id
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
  source_security_group_id = aws_security_group.gp2gp-message-handler-ecs-task-sg.id
}

data "aws_ssm_parameter" "service-to-mq-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-infra/service-to-mq-sg-id"
}

resource "aws_security_group_rule" "gp2gp-message-handler-to-mq" {
  type = "ingress"
  protocol            = "tcp"
  from_port           = "61617"
  to_port             = "61617"
  security_group_id = data.aws_ssm_parameter.service-to-mq-sg-id.value
  source_security_group_id = aws_security_group.gp2gp-message-handler-ecs-task-sg.id
}

data "aws_ssm_parameter" "service-to-repo-to-gp-sg-id" {
  name = "/repo/${var.environment}/output/prm-deductions-repo-to-gp/service-to-repo-to-gp-sg-id"
}

resource "aws_security_group_rule" "gp2gp-message-handler-to-repo-to-gp" {
  type = "ingress"
  protocol = "TCP"
  from_port = 443
  to_port = 443
  security_group_id = data.aws_ssm_parameter.service-to-repo-to-gp-sg-id.value
  source_security_group_id = aws_security_group.gp2gp-message-handler-ecs-task-sg.id
}

resource "aws_ecs_cluster" "gp2gp_message_handler_ecs_cluster" {
  name = "${var.environment}-${var.component_name}-ecs-cluster"

  tags = {
    Name = "${var.environment}-${var.component_name}"
    Environment = var.environment
    CreatedBy = var.repo_name
  }
}

resource "aws_security_group" "gp2gp-message-handler-ecs-task-sg" {
  name        = "${var.environment}-${var.component_name}-ecs-task-sg"
  vpc_id      = data.aws_ssm_parameter.deductions_private_vpc_id.value

  egress {
    description = "Allow All Outbound"
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.environment}-gp2gp-message-handler-ecs-task-sg"
    CreatedBy   = var.repo_name
    Environment = var.environment
  }
}


