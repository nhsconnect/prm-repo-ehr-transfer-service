resource "aws_cloudwatch_metric_alarm" "repo_incoming_age_of_message" {
  alarm_name          = "${var.environment}-${var.component_name}-repo-incoming-approx-age-of-oldest-message"
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.threshold_approx_age_oldest_message
  evaluation_periods  = "1"
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = local.sqs_namespace
  alarm_description   = "Alarm to alert approximate time for message in the queue"
  statistic           = "Maximum"
  period              = var.period_of_age_of_message_metric
  dimensions          = {
    QueueName = aws_sqs_queue.repo_incoming.name
  }
  alarm_actions       = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [data.aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_metric_alarm" "ehr_complete_age_of_message" {
  alarm_name          = "${var.environment}-${var.component_name}-ehr-complete-approx-age-of-oldest-message"
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.threshold_approx_age_oldest_message
  evaluation_periods  = "1"
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = local.sqs_namespace
  alarm_description   = "Alarm to alert approximate time for message in the queue"
  statistic           = "Maximum"
  period              = var.period_of_age_of_message_metric
  dimensions          = {
    QueueName = aws_sqs_queue.ehr_complete.name
  }
  alarm_actions       = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [data.aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_metric_alarm" "large_ehr_age_of_message" {
  alarm_name          = "${var.environment}-${var.component_name}-large-ehr-approx-age-of-oldest-message"
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.threshold_approx_age_oldest_message
  evaluation_periods  = "1"
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = local.sqs_namespace
  alarm_description   = "Alarm to alert approximate time for message in the queue"
  statistic           = "Maximum"
  period              = var.period_of_age_of_message_metric
  dimensions          = {
    QueueName = aws_sqs_queue.large_ehr.name
  }
  alarm_actions       = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [data.aws_sns_topic.alarm_notifications.arn]
}


resource "aws_cloudwatch_metric_alarm" "small_ehr_age_of_message" {
  alarm_name          = "${var.environment}-${var.component_name}-small-ehr-approx-age-of-oldest-message"
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.threshold_approx_age_oldest_message
  evaluation_periods  = "1"
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = local.sqs_namespace
  alarm_description   = "Alarm to alert approximate time for message in the queue"
  statistic           = "Maximum"
  period              = var.period_of_age_of_message_metric
  dimensions          = {
    QueueName = aws_sqs_queue.small_ehr.name
  }
  alarm_actions       = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [data.aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_metric_alarm" "large_message_fragments_age_of_message" {
  alarm_name          = "${var.environment}-${var.component_name}-large-message-fragments-approx-age-of-oldest-message"
  comparison_operator = "GreaterThanThreshold"
  threshold           = var.threshold_approx_age_oldest_message
  evaluation_periods  = "1"
  metric_name         = "ApproximateAgeOfOldestMessage"
  namespace           = local.sqs_namespace
  alarm_description   = "Alarm to alert approximate time for message in the queue"
  statistic           = "Maximum"
  period              = var.period_of_age_of_message_metric
  dimensions          = {
    QueueName = aws_sqs_queue.large_message_fragments.name
  }
  alarm_actions       = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions          = [data.aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_metric_alarm" "negative_acks_size" {
  alarm_name                = "${var.environment}-${var.component_name}-negative-acks-size"
  comparison_operator       = "GreaterThanThreshold"
  threshold                 = "0"
  evaluation_periods        = "1"
  metric_name               = "NumberOfMessagesSent"
  namespace                 = local.sqs_namespace
  alarm_description         = "Alarm to alert messages landed dlq"
  statistic                 = "Maximum"
  period                    = "300"
  dimensions = {
    QueueName = aws_sqs_queue.negative_acks.name
  }
  alarm_actions             = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions                = [data.aws_sns_topic.alarm_notifications.arn]
}

resource "aws_cloudwatch_metric_alarm" "parsing_dlq_size" {
  alarm_name                = "${var.environment}-${var.component_name}-parsing-dlq-size"
  comparison_operator       = "GreaterThanThreshold"
  threshold                 = "0"
  evaluation_periods        = "1"
  metric_name               = "NumberOfMessagesSent"
  namespace                 = local.sqs_namespace
  alarm_description         = "Alarm to alert messages landed dlq"
  statistic                 = "Maximum"
  period                    = "300"
  dimensions = {
    QueueName = aws_sqs_queue.parsing_dlq.name
  }
  alarm_actions             = [data.aws_sns_topic.alarm_notifications.arn]
  ok_actions                = [data.aws_sns_topic.alarm_notifications.arn]
}

