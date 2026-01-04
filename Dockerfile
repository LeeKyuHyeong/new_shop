FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# GitHub Actions에서 빌드된 WAR 복사
COPY target/*.war app.war

# 메모리 제한 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]