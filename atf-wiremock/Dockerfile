FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/atf-wiremock.jar app.jar
EXPOSE 1398
VOLUME ["/app/mappings", "/app/jms-mappings", "/app/__files", "/app/velocity"]
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar", "--spring.profiles.active=docker"]
