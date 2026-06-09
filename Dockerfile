#빌드
FROM eclipse-temurin:21 as builder

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew
RUN ./gradlew bootjar

#실행
FROM eclipse-temurin:21 as runtime

RUN addgroup --system --gid 1000 worker
RUN adduser --system --uid 1000 --ingroup worker --disabled-password worker
USER worker:worker

COPY --from=builder build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]