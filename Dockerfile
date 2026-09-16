FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon \
    && find build/libs -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" -exec cp {} build/libs/app.jar \;

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin appuser

COPY --from=build /app/build/libs/app.jar app.jar

RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]