# --- Build stage -----------------------------------------------------------
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /workspace

# 依存関係のみを先にコピーしてキャッシュを効かせる
COPY build.gradle settings.gradle ./
COPY src ./src

RUN gradle bootJar --no-daemon -x test

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S buzzapp && adduser -S buzzapp -G buzzapp
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
RUN chown -R buzzapp:buzzapp /app
USER buzzapp

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -q -O - http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
