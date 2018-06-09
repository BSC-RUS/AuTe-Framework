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

## GIT configuration ##
Configure git before commiting.
Execute following commands from command line:
```
git config user.name "John Doe"
git config user.email johndoe@example.com
```

Check configuration with `git config -l`

## Requirements ##
 * JDK 1.8
 * Apache Maven 3
 * NodeJS
 * npm

## Build and run AuTe Framework ##
1. Clone project
1. Build project with install nodeJS packages `mvn clean package -P npm-install`
1. Create working directory for application
1. Place in working directory:
    * *atf-application-version.jar*
    * *env.yml* from [sample](atf-application/src/main/resources/env.yml.sample)
    * run script [run.bat](atf-application/src/main/resources/run.bat) or [run.sh](atf-application/src/main/resources/run.sh)
1. Run in working directory `java -Dloader.path=lib/ -jar atf-application-version.jar` or use run script

### env.yml description ###    
 * projectsDirectoryPath - path to tests repository
 * historyLimit - 
 * projectStandMap:
    * PROJECT_CODE_1:
        * serviceUrl - URL of the application under test
        * dataBase:
            * url - URL to database
            * user - database user
            * password - database password
        * wireMockUrl - wiremock URL
    * PROJECT_CODE_2: ...

## Build and run wiremock ##
1. Clone project
1. Build project with install nodeJS packages `mvn clean pacakge -P npm-install`
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

## IBM MQ mocking ##
For mocking IBM MQ you have to find and download
* com.ibm.mq.allclient.jar
* jms.jar
and put it in `/lib` folder or another.
You can find ibm libs in fix pack [Fix Pack 8.0.0.5 for WebSphere MQ v8](http://www-01.ibm.com/support/docview.wss?uid=swg21982686) or copy it from installed IBM product.
You have to put these libs in application classpath for atf-application and for atf-wiremock.