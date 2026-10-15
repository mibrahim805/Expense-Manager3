# Deploy RupeyaWise API to AWS ECS Express Mode

This deploys the existing Django Docker image to Amazon ECS Express Mode and keeps PostgreSQL in Neon. It does not change the existing Kubeletto `reelhouse` app.

## Before deploying

- Use AWS region `us-east-2` (Ohio), matching the Neon project region.
- Install Docker and AWS CLI v2, and sign in with an AWS identity that can create ECR, ECS, IAM, Secrets Manager, CloudWatch Logs, and load-balancer resources. Do not use root access keys.
- ECS Express Mode provisions Fargate, an internet-facing Application Load Balancer, and related resources. These can incur charges even when traffic is low; AWS credits are finite and may not cover six months. Set a billing budget/alert before creating the service. AWS currently documents a 256 CPU-unit / 512 MiB task default; keep the service to one task unless usage requires more.

## 1. Build and push the image to ECR

Run from the repository root after configuring AWS CLI credentials:

```sh
AWS_REGION=us-east-2
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPOSITORY=rupeyawise-api
ECR_URI=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}
IMAGE_TAG=$(git rev-parse --short HEAD)

aws ecr create-repository --repository-name "$ECR_REPOSITORY" --region "$AWS_REGION"
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
docker buildx build --platform linux/amd64 --tag "${ECR_URI}:${IMAGE_TAG}" --push .
```

Create the ECR repository only once. For future releases, build and push a new tag, then update the ECS Express service to use that image tag.

## 2. Store secrets outside Git

In AWS Secrets Manager, create separate secrets for:

- `rupeyawise/DATABASE_URL` — the pooled Neon connection string.
- `rupeyawise/DJANGO_SECRET_KEY` — a new, random Django key.

Do not put secret values in this repository, Android/Gradle configuration, a Docker image, or a shell command. In the ECS task configuration, reference these secrets as container secrets named `DATABASE_URL` and `DJANGO_SECRET_KEY`. The ECS task execution role must be allowed to read those two secrets.

Before deploying, make sure the task execution role has `AmazonECSTaskExecutionRolePolicy` plus an inline policy limited to these secret ARNs:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": "secretsmanager:GetSecretValue",
    "Resource": ["<DATABASE_URL secret ARN>", "<DJANGO_SECRET_KEY secret ARN>"]
  }]
}
```

If using a customer-managed KMS key for the secrets, also grant `kms:Decrypt` for that key. The ECS infrastructure role is separate; the Express Mode console can create it during setup.

## 3. Create the ECS Express service

In the AWS console, open **Amazon ECS → Express mode**, then create a service. The first-run flow can create the ECS roles for you. Configure:

- Service name: `rupeyawise-api`
- Region: `us-east-2`
- Image: the ECR image URI from step 1
- Container port: `8080`
- Health check path: `/health/`
- CPU and memory: start at `256` CPU units and `512` MiB
- Minimum and maximum tasks: `1` and `1` initially
- Environment variables:
  - `DJANGO_DEBUG=False`
  - `DJANGO_ALLOWED_HOSTS=rupeyawise-api.ecs.us-east-2.on.aws`
  - `DJANGO_CSRF_TRUSTED_ORIGINS=https://rupeyawise-api.ecs.us-east-2.on.aws`
  - `DJANGO_SECURE_HSTS_SECONDS=86400`
- Container secrets: map the two Secrets Manager values from step 2 to `DATABASE_URL` and `DJANGO_SECRET_KEY`.

The task execution role also needs `secretsmanager:GetSecretValue` access to those two secret ARNs (and `kms:Decrypt` if you use a customer-managed KMS key). Add only the required secret ARNs to its permissions; do not grant broad access to every secret in the account.

If AWS assigns a different URL, update `DJANGO_ALLOWED_HOSTS` (hostname only) and `DJANGO_CSRF_TRUSTED_ORIGINS` (`https://` plus hostname), then redeploy. When the service is active, confirm `https://<service-host>/health/` returns `{"status":"ok"}`.

## 4. Point Android at the deployed API

Once the HTTPS service URL is confirmed:

```sh
./gradlew :androidApp:installDebug -PrupeyawiseApiBaseUrl=https://<service-host>/
```

Use the same HTTPS URL when building a release. Do not ship AWS credentials, Neon credentials, or Django secrets in the Android app.

## Operations and six-month shutdown

Pushes to GitHub do not automatically update this ECS service. For each release, build and push a new ECR image tag, then update the ECS Express service to that image. Follow the ECS deployment guide linked below.

Before the six-month AWS Free plan/credit period ends, export the Neon database and either migrate the API to another host or explicitly decide to upgrade AWS. Deleting/stopping the ECS service alone does not remove every billable resource; review and remove its load balancer, networking, ECR images, logs, and secrets when retiring it.

See [AWS ECS Express Mode deployment guide](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/express-service-getting-started.html) and [AWS cost and Free Tier information](https://aws.amazon.com/free/free-tier-faqs/).
