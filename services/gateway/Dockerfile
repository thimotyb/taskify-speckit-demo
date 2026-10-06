# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY config config
COPY services/project-service/pom.xml services/project-service/pom.xml
COPY services/task-service/pom.xml services/task-service/pom.xml
COPY services/gateway/pom.xml services/gateway/pom.xml
COPY services/gateway/src services/gateway/src
RUN mvn -q -pl services/gateway -DskipTests package

# Runtime stage: non-root user
FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 taskify
USER taskify
COPY --from=build /workspace/services/gateway/target/gateway-*.jar /app/app.jar
ENV SPRING_PROFILES_ACTIVE=compose
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
