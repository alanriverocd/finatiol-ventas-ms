FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY finatiol-common/pom.xml ./finatiol-common/pom.xml
COPY finatiol-common/src ./finatiol-common/src
RUN mvn -f finatiol-common/pom.xml install -DskipTests -q
COPY finatiol-ventas-ms/pom.xml ./finatiol-ventas-ms/pom.xml
COPY finatiol-ventas-ms/src ./finatiol-ventas-ms/src
RUN mvn -f finatiol-ventas-ms/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/finatiol-ventas-ms/target/*.jar app.jar
RUN groupadd -r appgroup && useradd -r -g appgroup -u 1001 appuser
USER appuser
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
