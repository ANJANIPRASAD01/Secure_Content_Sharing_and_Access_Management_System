FROM openjdk:17-jdk as builder
WORKDIR /app
COPY . .
RUN cd backend && mvn clean package -DskipTests

FROM openjdk:17-jre
COPY --from=builder /app/backend/target/filevault-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
