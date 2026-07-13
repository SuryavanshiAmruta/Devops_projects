variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project Name"
  type        = string
  default     = "terraform-project"
}

variable "vpc_cidr" {
  default = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  default = "10.0.1.0/24"
}

variable "availability_zone" {
  default = "us-east-1a"
}

variable "instance_type" {
  default = "t3.micro"
}

variable "key_name" {
  description = "AWS Key Pair Name"
  type        = string
  default     = "terraform-key"
}

variable "ami_id" {
  description = "Ubuntu AMI ID"
  type        = string

  # Ubuntu 24.04 LTS (us-east-1)
  default = "ami-0f8a61b66d1accaee"
}