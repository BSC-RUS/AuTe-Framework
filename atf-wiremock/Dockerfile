FROM openjdk:8-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
RUN mkdir -p "mount/logs"
RUN mkdir -p "mount/conf"
COPY src/main/resources/application.properties.sample ./mount/conf/application.properties
COPY properties.yml ./mount/conf/properties.yml
EXPOSE 1398
VOLUME ["/app/mount", "/app/mappings"]
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar", "--spring.config.location=file:./mount/conf/application.properties"]