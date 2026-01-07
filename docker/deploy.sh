#!/bin/bash
set -e

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

LOG_FILE="$HOME/snapng/deploy.log"
mkdir -p "$HOME/snapng"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] 🚀 Starting deployment..." | tee -a $LOG_FILE

# Load environment variables from GitHub secrets
export DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}"
export JWT_SECRET="${JWT_SECRET}"
export SENDGRID_API_KEY="${SENDGRID_API_KEY}"
export CLOUDINARY_CLOUD_NAME="${CLOUDINARY_CLOUD_NAME}"
export CLOUDINARY_API_KEY="${CLOUDINARY_API_KEY}"
export CLOUDINARY_API_SECRET="${CLOUDINARY_API_SECRET}"
export SPRING_MAIL_PASSWORD="${SPRING_MAIL_PASSWORD}"
export AWS_S3_BUCKET_NAME="${AWS_S3_BUCKET_NAME}"
export AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID}"
export AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY}"
export FIREBASE_CONFIG="${FIREBASE_CONFIG}"

# Pull latest image
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] Pulling latest Docker image...${NC}" | tee -a $LOG_FILE
docker pull $DOCKERHUB_USERNAME/snapng:staging-latest 2>&1 | tee -a $LOG_FILE

# Stop and remove old container
CONTAINER_NAME="snapng-staging"
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] Stopping old container...${NC}" | tee -a $LOG_FILE
    docker stop $CONTAINER_NAME 2>&1 | tee -a $LOG_FILE || true
    docker rm $CONTAINER_NAME 2>&1 | tee -a $LOG_FILE || true
fi

# Deploy with docker-compose
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] Starting new container...${NC}" | tee -a $LOG_FILE
cd $HOME/snapng
docker-compose up -d 2>&1 | tee -a $LOG_FILE

# Wait for health check
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] Waiting for application health check...${NC}" | tee -a $LOG_FILE
sleep 45

# Verify deployment
if docker ps | grep -q $CONTAINER_NAME; then
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] ✅ Deployment successful!${NC}" | tee -a $LOG_FILE
    docker logs $CONTAINER_NAME | tail -20 >> $LOG_FILE
else
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ❌ Deployment failed!${NC}" | tee -a $LOG_FILE
    exit 1
fi

# Cleanup old images (saves disk space)
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] Cleaning up old images...${NC}" | tee -a $LOG_FILE
docker image prune -f 2>&1 | tee -a $LOG_FILE