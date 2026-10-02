FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B -DskipTests package
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.war /app/qaute.war
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/qaute.war"]
