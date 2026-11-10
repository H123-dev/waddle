#depnds

FROM eclipse-temurin:21-jdk-alpine AS depnds
RUN apk add --no-cache maven
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

#app

FROM depnds AS builder
COPY src ./src
RUN mvn  clean package -DskipTests


#runner
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

COPY --from=builder  /build/target/*.jar app.jar


ENTRYPOINT ["java","-jar","app.jar"]