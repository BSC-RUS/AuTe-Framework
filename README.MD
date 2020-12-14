# AuTe Framework - Auto Test Framework #

## Projects ##
Name | Description
:---:|:---:
[atf-application](atf-application) | AuTe Framework application
[atf-wiremock](atf-wiremock) | Wiremock application
[atf-executor](atf-executor) | Test execution logic
[atf-commons](atf-commons) | Common components for all projects
[atf-application-ui](atf-application-ui) | Frontend for AuTe Framework
[atf-wiremock-ui](atf-wiremock-ui) | Frontend for Wiremock

## Requirements ##
 * JDK 1.8
 * Apache Maven 3
 * NodeJS
 * npm

## Build and run AuTe Framework ##
1. Clone project
1. Build project with install nodeJS packages `mvn clean package -P npm-install`
1. Create a working directory for application
1. Place in working directory:
    * *atf-application-version.jar*
    * *env.yml* from [sample](atf-application/src/main/resources/env.yml.sample)
    * run script [run.bat](atf-application/src/main/resources/run.bat) or [run.sh](atf-application/src/main/resources/run.sh)
1. Run in working directory `java -Dloader.path=lib/ -jar atf-application-version.jar` or use run script

### env.yml description ###    
 * projectsDirectoryPath - path to tests repository
 * historyLimit - number of stored test runs in history
 * mqCheckCount - check count of mqRequests
 * mqCheckInterval - interval in milliseconds between checks 
 * projectStandMap:
    * PROJECT_CODE_1:
        * serviceUrl - URL of the application under test
        * dataBase:
            * url - URL to database
            * user - database user
            * password - database password
        * wireMockUrl - wiremock URL
    * PROJECT_CODE_2: ...

## Build and run Wiremock ##
1. Clone project
1. Build project with install nodeJS packages `mvn clean package -P npm-install`
1. Create working directory
1. Place in working directory:
    * *atf-wiremock-version.jar*
    * create *application.properties* file
    * create wiremock mappings configuration file
1. Run in working directory `java -Dloader.path=lib/ -Dfile.encoding=UTF-8 -jar atf-wiremock-version.jar`

### application.properties file description ###
Property | Description
:---:|:---:
server.port | application run port
logging.file | log file name
mq.manager | JMS provider name
mq.host | JMS host
mq.port | JMS port
mq.username | JMS username
mq.password | JSM password
mq.default.destination.queue.name |
properties.yaml.file | Wiremock mappings configuration file name
test.id.header.name |

### Wiremock mappings configuration file description ###
Wiremock mapping configuration file has json format.

Property | Description
:---:|:---:
id | mock's id
request | mock's request patter
request.url | JMS provider name
request.method | JMS host
request.headers | JMS port
request.bodyPatterns | JMS username
response | mock's response
response.status | response status
response.body | response body
response.headers | response headers
uuid | mock's uuid

## Run AuTe Framework in Docker ##
1. Build docker images with command `mvn clean package -P docker` or use existed images from [DockerHub](https://hub.docker.com/r/bscmsc/aute-framework/tags)
1. Copy [docker-compose.yml.sample](docker-compose.yml.sample) to your local machine and rename it to docker-compose.yml
1. Modify image tags, volumes and ports if needed in local docker-compose.yml
1. Change `projectsDirectoryPath` in your `env.yml` to `/app/repository/<AT_directory>`
1. Run containers with command `docker-compose up` to run both ATF and Wiremock applications or `docker-compose up atf` to run ATF only
1. Go to http://localhost:8080 to check running ATF application

## IBM MQ mocking ##
For mocking IBM MQ you have to find and download
* com.ibm.mq.allclient.jar
* jms.jar
and put it in `/lib` folder or another.
You can find ibm libs in fix pack [Fix Pack 8.0.0.5 for WebSphere MQ v8](http://www-01.ibm.com/support/docview.wss?uid=swg21982686) or copy it from installed IBM product.
You have to put these libs in application classpath for atf-application and for atf-wiremock.

## PERFORMANCE ##

Application may be slow if not set properties: 

mqCheckCount: 3
mqCheckInterval: 1000

in file env.yml
