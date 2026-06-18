# Stage 1: build
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src src
RUN ./mvnw package -DskipTests -q

# Stage 2: run
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
VOLUME /app/uploads
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]
