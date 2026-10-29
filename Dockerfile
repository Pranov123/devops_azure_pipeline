# The jar is built and tested by Maven in the Jenkins pipeline before this step,
# so the image only needs a Java runtime and the packaged application.
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="devops-azure-pipeline" \
      org.opencontainers.image.source="https://github.com/Pranov123/devops_azure_pipeline"

# Run as a non-root user (numeric UID so Kubernetes runAsNonRoot can verify it)
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app
WORKDIR /app

COPY target/app.jar app.jar
RUN chown app:app app.jar
USER 10001

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
