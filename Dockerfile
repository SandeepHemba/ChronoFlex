# # Build stage
# FROM maven:3.9.6-eclipse-temurin-17 AS build
# WORKDIR /app
# COPY . .
# RUN mvn clean package -Dmaven.test.skip=true

# # Run stage
# FROM eclipse-temurin:17-jdk
# WORKDIR /app
# COPY --from=build /app/target/*.jar app.jar
# #CMD ["java", "-jar", "app.jar"]
# CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]

# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port (IMPORTANT for Render)
EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT} --server.address=0.0.0.0"]
