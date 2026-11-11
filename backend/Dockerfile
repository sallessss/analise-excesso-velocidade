FROM maven:3.9.11-eclipse-temurin-21-alpine AS compilacao

WORKDIR /aplicacao

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /aplicacao

COPY --from=compilacao /aplicacao/target/analise-velocidade-0.0.1-SNAPSHOT.jar aplicacao.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "aplicacao.jar"]
