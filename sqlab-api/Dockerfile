# Stage 1: Build
FROM azul/zulu-openjdk-alpine:25 AS builder
WORKDIR /app
# Install Maven manually as it might not be in the base image
RUN apk add --no-cache maven
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM azul/zulu-openjdk-alpine:25-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
