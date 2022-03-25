### prm-repo-ehr-transfer-service

This is an implementation of a component to handle the receiving of the GP2GP message set used to transfer a patient's
Electronic Health Record between GP Practices.

## Prerequisites

- Java v11 LTS
- Gradle 7.3.2

### AWS helpers

This repository imports shared AWS helpers
from [prm-deductions-support-infra](https://github.com/nhsconnect/prm-deductions-support-infra/). They can be
found `utils` directory after running any task from `tasks` file.

## Set up

### Running the application

In IntelliJ editor, use the green `play` button next to the main class `Gp2gpmessagehandlerApplication` to run the
application

In your terminal with `./gradlew bootRun`.

This application relies on the queues to send/receive messages. They are spun up with `docker-compose-itest.yml` when
running the tests in `dojo`. You can access the queues using the Active MQ console on: `http://localhost:8161/`

### Running the tests

Run the unit tests with dojo

1. Enter ` dojo `
2. `./tasks _test_unit`

In your terminal with
`./gradlew test`

Run the integration tests with dojo

1. Enter ` dojo `
2. `./tasks _test_integration`

Alternatively, you can use `play` button next to each test in IntelliJ (your IDE)

Run the coverage tests with a Dojo container

1. Enter ` dojo `
2. `./tasks _test_coverage`

Run the dependency check tests with a Dojo container

1. Enter ` dojo `
2. `./tasks _dep`

To run all the checks before committing with one command

1. Enter `dojo `
2. `./tasks _test_all`

### Config

If you need to add any new configuration items, update the `src/main/resources/application.properties` file per
environment as well as add the environment variables in `./tasks` `configure_local_envariables`. Note that `test`
directory has its own `application.properties` file used in the test suite.

| Parameters          | SSM Parameter                                                             |
|---------------------|---------------------------------------------------------------------------|
| active-mq.broker-url| /repo/${NHS_ENVIRONMENT}/output/prm-deductions-infra/amqp-endpoint-0      |
| active-mq.queue     | /repo/${NHS_ENVIRONMENT}/output/prm-deductions-infra/amqp-endpoint-1      |
| active-mq.username  | /repo/${NHS_ENVIRONMENT}/user-input/mq-admin-username                     |
| active-mq.username  | /repo/${NHS_ENVIRONMENT}/user-input/mq-app-username                       | - to access user interface
| active-mq.password  | /repo/${NHS_ENVIRONMENT}/user-input/mq-admin-password                     |
| active-mq.password  | /repo/${NHS_ENVIRONMENT}/user-input/mq-app-password                       |

Ensure you have VPN connection set up to both `dev` and `test` environments:
[CLICK HERE](https://gpitbjss.atlassian.net/wiki/spaces/TW/pages/1832779966/VPN+for+Deductions+Services)

### Setup

In AmazonMQ settings for either the `dev` or `test` provision. Edit the `deductor-amq-broker-${NHS_ENVIRONMENT}`
security group inbound rules. Add new rule that allows All TCP from the `${NHS_ENVIRONMENT} VPN VM security group`,
apply before running the following:

```
// Starts the server locally using `.env`
$ NHS_ENVIRONMENT=test 
```

## Access to AWS

In order to get sufficient access to work with terraform or AWS CLI:

Make sure to unset the AWS variables:

```
unset AWS_ACCESS_KEY_ID
unset AWS_SECRET_ACCESS_KEY
unset AWS_SESSION_TOKEN
```

As a note, the following set-up is based on the README of assume-role [tool](https://github.com/remind101/assume-role)

Set up a profile for each role you would like to assume in `~/.aws/config`, for example:

```
[profile default]
region = eu-west-2
output = json

[profile admin]
region = eu-west-2
role_arn = <role-arn>
mfa_serial = <mfa-arn>
source_profile = default
```

The `source_profile` needs to match your profile in `~/.aws/credentials`.

```
[default]
aws_access_key_id = <your-aws-access-key-id>
aws_secret_access_key = <your-aws-secret-access-key>
```

## Assume role with elevated permissions

### Install `assume-role` locally:

`brew install remind101/formulae/assume-role`

Run the following command with the profile configured in your `~/.aws/config`:

`assume-role admin`

### Run `assume-role` with dojo:

Run the following command with the profile configured in your `~/.aws/config`:

`eval $(dojo "echo <mfa-code> | assume-role admin"`

Run the following command to confirm the role was assumed correctly:

`aws sts get-caller-identity`

## AWS SSM Parameters Design Principles

When creating the new ssm keys, please follow the agreed convention as per the design specified below:

* all parts of the keys are lower case
* the words are separated by dashes (`kebab case`)
* `env` is optional

### Design:

Please follow this design to ensure the ssm keys are easy to maintain and navigate through:

| Type               | Design                                  | Example                                               |
| -------------------| ----------------------------------------| ------------------------------------------------------|
| **User-specified** |`/repo/<env>?/user-input/`               | `/repo/${var.environment}/user-input/db-username`     |
| **Auto-generated** |`/repo/<env>?/output/<name-of-git-repo>/`| `/repo/output/prm-deductions-base-infra/root-zone-id` |
