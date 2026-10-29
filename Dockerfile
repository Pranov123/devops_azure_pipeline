# The jar is built and tested by Maven in the Jenkins pipeline before this step,
# so the image only needs a Java runtime and the packaged application.
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="devops-aws-pipeline" \
      org.opencontainers.image.source="https://github.com/Pranov123/devops_aws_pipeline"

# Run as a non-root user
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY target/app.jar app.jar
RUN chown app:app app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
