FROM azul/zulu-openjdk-alpine:25
WORKDIR /app
# Install Maven for initial dependency download, though mvnw is preferred
RUN apk add --no-cache maven
COPY . .
# Ensure Maven wrapper is executable
RUN chmod +x mvnw
EXPOSE 8081
# Run using Spring Boot devtools if available, or just boot:run
CMD ["./mvnw", "spring-boot:run"]
