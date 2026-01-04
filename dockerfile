# 빌드 스테이지
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 업로드 디렉토리 생성
RUN mkdir -p /app/uploads

# JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 메모리 제한 설정 (서버 RAM 1G 고려)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]