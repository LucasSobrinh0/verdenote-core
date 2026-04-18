FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system verdenote && adduser --system --ingroup verdenote verdenote
COPY --from=build /app/build/libs/*.jar app.jar

USER verdenote
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
