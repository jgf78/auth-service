FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --chown=appuser:appuser target/auth-service-*.jar app.jar

USER appuser

EXPOSE 8087

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]