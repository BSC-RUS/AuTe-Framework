FROM openjdk:8-jre-alpine
WORKDIR /app
RUN mkdir -p "conf"
COPY src/main/resources/application.yaml.docker ./conf/application.yaml
COPY target/atf-wiremock.jar app.jar
EXPOSE 1398
VOLUME ["/app/conf", "/app/mappings", '/app/jms-mappings', '/app/__files', '/app/velocity']
CMD ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar", "--spring.config.location=file:./conf/application.yaml"]
