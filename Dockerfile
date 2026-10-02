# Stage 1: build with Maven (this heavy image is not part of the final result)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# --mount=type=cache keeps Maven dependencies between builds: they are downloaded only once
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# Stage 2: just the JRE and the .jar (much lighter image)
FROM eclipse-temurin:21-jre
WORKDIR /app

# The service doesn't run as root
RUN useradd --system --uid 1001 rentamovil
COPY --from=build /app/target/rtm-fleet-maintenance-*.jar app.jar
USER rentamovil

EXPOSE 3002
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
    CMD curl -fs http://localhost:3002/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
