# 1️⃣ 빌드 단계
FROM gradle:8.5-jdk21 AS build

WORKDIR /app
COPY . .

RUN ./gradlew build -x test

# 2️⃣ 실행 단계
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]