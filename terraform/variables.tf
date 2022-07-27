variable "region" {
  type    = string
  default = "eu-west-2"
}

variable "repo_name" {
  type    = string
  default = "prm-repo-ehr-transfer-service"
}

variable "environment" {}

variable "component_name" {
  type    = string
  default = "ehr-transfer-service"
}
variable "task_image_tag" {}
variable "task_cpu" {}
variable "task_memory" {}

variable "service_desired_count" {}

variable "log_level" {
  type    = string
  default = "debug"
}

variable "environment_dns_zone" {
  description = "The environment-specific labels of the dns zone name, e.g. 'prod' or 'dev.non-prod'"
}
