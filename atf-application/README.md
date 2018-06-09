# AuTe Framework-application #
Application for creating test-cases, running tests, generating reports.

### Project settings ###
Projects and test-cases are stored in yml files.
Projects' folder is specified in `env.yml` file (look `env.yml.sample`).

### Run ###
To run application use `autotester-4.0.0.jar`.
You can use `run.bat` or `run.sh` depending on your operating system:
```
java -Dloader.path=lib/ -Dfile.encoding=UTF-8 -jar autotester-4.0.2.jar --server.port=8080
```
Parameter `--server.port` specifies application port.
When application is started, go to http://localhost:8080/ with browser.