# ---- build ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---- runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 agromotor
COPY --from=build /app/target/agromotor-*.jar app.jar
USER agromotor
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
