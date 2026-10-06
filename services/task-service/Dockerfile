# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY config config
COPY services/project-service/pom.xml services/project-service/pom.xml
COPY services/task-service/pom.xml services/task-service/pom.xml
COPY services/gateway/pom.xml services/gateway/pom.xml
COPY services/task-service/src services/task-service/src
RUN mvn -q -pl services/task-service -DskipTests package

# Runtime stage: non-root user, data volume mounted at /data/task-service
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 taskify && mkdir -p /data/task-service && chown taskify /data/task-service
USER taskify
COPY --from=build /workspace/services/task-service/target/task-service-*.jar /app/app.jar
ENV TASKIFY_DATA_DIR=/data SPRING_PROFILES_ACTIVE=compose
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
