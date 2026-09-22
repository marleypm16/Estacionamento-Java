FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -q -DskipTests dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --create-home parking
COPY --from=build /workspace/target/estacionamento-api-*.jar app.jar
USER parking
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
