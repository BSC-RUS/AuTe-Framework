FROM openjdk:8-jre
WORKDIR /app
COPY target/atf-application.jar ./app.jar
RUN mkdir -p ./{repository, config, allure-plugins}
VOLUME ["/app/config", "/app/repository", "/app/allure-plugins"]
EXPOSE 8080
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar", "--spring.profiles.active=docker"]