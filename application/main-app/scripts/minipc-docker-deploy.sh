#!/bin/bash

# 배포 로그 파일 설정
DEPLOY_LOG="/home/hong/app/yapp-main-application/deploy.log"
COMPOSE_FILE="docker-compose.minipc.yml"

# 현재 디렉토리 이동
cd /home/hong/app/yapp-main-application

# 새로운 Docker 이미지 빌드
echo "Docker 이미지 빌드 시작 ..." >> $DEPLOY_LOG
docker-compose -f $COMPOSE_FILE build >> $DEPLOY_LOG 2>&1

if [ $? -ne 0 ]; then
  echo "Docker 이미지 빌드 실패!" >> $DEPLOY_LOG
  exit 1
fi

echo "Docker 이미지 빌드 완료!" >> $DEPLOY_LOG

# 기존 컨테이너 중지 및 삭제 (존재 여부 확인 후 중지/삭제)
if [ "$(docker ps -a -q -f name=yapp-main-application-container)" ]; then
  echo "기존 컨테이너 종료 및 삭제 ..." >> $DEPLOY_LOG
  docker stop -f $COMPOSE_FILE yapp-main-application-container >> $DEPLOY_LOG 2>&1
  docker rm -f $COMPOSE_FILE yapp-main-application-container --force >> $DEPLOY_LOG 2>&1
  if [ $? -ne 0 ]; then
    echo "기존 컨테이너 종료 및 삭제 실패!" >> $DEPLOY_LOG
    exit 1
  fi
else
  echo "삭제할 기존 컨테이너가 없습니다." >> $DEPLOY_LOG
fi

# 새로운 컨테이너 실행
echo "Docker 컨테이너 실행 시작 ..." >> $DEPLOY_LOG
docker-compose -f $COMPOSE_FILE up -d >> $DEPLOY_LOG 2>&1

if [ $? -ne 0 ]; then
  echo "Docker 컨테이너 실행 실패!" >> $DEPLOY_LOG
  exit 1
fi

echo "### Docker 컨테이너 실행 완료!" >> $DEPLOY_LOG