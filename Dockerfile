FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN apk add --no-cache curl

RUN addgroup -g 1001 -S spring \
 && adduser -u 1001 -S spring -G spring

USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=5 \
 CMD curl --fail http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
