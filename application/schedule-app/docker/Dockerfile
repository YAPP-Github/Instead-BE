### Docker 이미지를 생성할 때 기반이 되는 베이스 이미지를 설정한다.
FROM openjdk:17
### Dockerfile 내에서 사용할 변수 JAR_FILE을 정의한다.
#WORKDIR /app
#ARG JAR_FILE=/build/libs/animal-meeting-blue-green-app.jar
ARG JAR_FILE=/yapp-upload-schedule-application.jar
### JAR_FILE 경로에 해당하는 파일을 Docker 이미지 내부로 복사한다.
COPY ${JAR_FILE} yapp-upload-schedule-application.jar
### Docker 컨테이너가 시작될 때 실행할 명령을 지정한다.
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "/yapp-upload-schedule-application.jar"]
