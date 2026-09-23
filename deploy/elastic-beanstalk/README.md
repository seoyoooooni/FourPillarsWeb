# Elastic Beanstalk backend deployment

This bundle deploys the backend ECR image to a single-container Elastic
Beanstalk environment in `ap-northeast-2`.

## Source bundle

Create the upload archive from inside this directory so that
`Dockerrun.aws.json` and `.ebextensions` are at the root of the zip file:

```sh
cd deploy/elastic-beanstalk
zip -r fourpillars-backend-eb.zip Dockerrun.aws.json .ebextensions
```

## Required EC2 instance profile permissions

The Elastic Beanstalk EC2 instance profile must have:

- `AWSElasticBeanstalkWebTier`
- `AmazonEC2ContainerRegistryReadOnly`
- permission to call `ssm:GetParameter` for
  `arn:aws:ssm:ap-northeast-2:504857725459:parameter/fourpillars/prod/*`

The repository includes `iam-parameter-read-policy.json`, which can be used
as the role's inline policy document.

The role trust policy must allow `ec2.amazonaws.com` to assume it.

## Environment choices

- Tier: Web server environment
- Platform: Docker running on 64bit Amazon Linux 2023
- Environment type: Single instance
- Architecture: x86_64
- Instance type: `t3.micro`
- VPC: the VPC containing the RDS instance
- EC2 subnet: a public subnet with automatic public IPv4 assignment
- Database subnet: keep RDS in its existing private subnets
- Health path: `/actuator/health`

Allow inbound TCP 5432 on the RDS security group only from the Elastic
Beanstalk EC2 instance security group. Do not make the database publicly
accessible.

After GitHub Actions has pushed the `latest` image, uploading a new source
bundle version (or redeploying the existing bundle) makes Elastic Beanstalk
pull the current image.
