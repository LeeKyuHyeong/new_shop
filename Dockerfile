# 실행 스테이지 (빌드 스테이지 제거)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# GitHub Actions에서 빌드된 WAR 파일을 복사
# (target 폴더 아래의 war 파일을 app.war로 복사)
COPY target/*.war app.war

# 메모리 제한 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]