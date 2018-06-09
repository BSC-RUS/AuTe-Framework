/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.autotester.repository.yaml.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.at.util.YamlUtils;
import ru.bsc.test.autotester.component.Translator;
import ru.bsc.test.autotester.utils.FileExtensionsUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by smakarov
 * 05.03.2018 13:56
 */
@Slf4j
public abstract class BaseYamlRepository {
    protected static final String MAIN_YML_FILENAME = "main.yml";
    protected static final String SCENARIO_YML_FILENAME = "scenario.yml";
    private static final String FILE_ENCODING = "UTF-8";
    private static final String REQUEST_JSON = "request.json";
    private static final String MQ_MOCK_RESPONSES_PATH = "mq-mock-responses";

    protected final Translator translator;

    public BaseYamlRepository(Translator translator) {
        this.translator = translator;
    }

    protected void saveScenarioToFiles(Scenario scenario, File scenarioFile) throws IOException {
        File scenarioRootDirectory = scenarioFile.getParentFile();
        for (int i = 0; i < scenario.getStepList().size(); i++) {
            int order = i + 1;
            saveStepToFiles(order, scenario.getStepList().get(i), scenarioRootDirectory);
        }
        String scenarioGroup = scenario.getScenarioGroup();
        scenario.setScenarioGroup(null);
        YamlUtils.dumpToFile(scenario, scenarioFile.getAbsolutePath());
        scenario.setScenarioGroup(scenarioGroup);
    }

    protected void loadStepFromFiles(Step step, File scenarioRootDirectory) {
        if (step.getRequestFile() != null && step.getRequest() == null) {
            step.setRequest(readFile(scenarioRootDirectory + "/" + step.getRequestFile()));
        }
        if (step.getExpectedResponseFile() != null && step.getExpectedResponse() == null) {
            step.setExpectedResponse(readFile(scenarioRootDirectory + "/" + step.getExpectedResponseFile()));
        }
        if (step.getMqMessageFile() != null && step.getMqMessage() == null) {
            step.setMqMessage(readFile(scenarioRootDirectory + "/" + step.getMqMessageFile()));
        }
        if (step.getMqMessages() != null) {
            step.getMqMessages().forEach(message -> {
                if (message.getMessageFile() != null && message.getMessage() == null) {
                    message.setMessage(readFile(scenarioRootDirectory + File.separator + message.getMessageFile()));
                }
            });
        }

        step.getMockServiceResponseList().forEach(mockServiceResponse -> {
            if (mockServiceResponse.getResponseBodyFile() != null && mockServiceResponse.getResponseBody() == null) {
                mockServiceResponse.setResponseBody(readFile(scenarioRootDirectory + "/" + mockServiceResponse.getResponseBodyFile()));
            }
        });

        step.getExpectedServiceRequests().forEach(expectedServiceRequest -> {
            if (expectedServiceRequest.getExpectedServiceRequestFile() != null && expectedServiceRequest.getExpectedServiceRequest() == null) {
                expectedServiceRequest.setExpectedServiceRequest(readFile(scenarioRootDirectory + "/" + expectedServiceRequest.getExpectedServiceRequestFile()));
            }
        });

        //TODO: удалить данную ветвь после прекращения поддержки старого формата хранения MQ моков
        Path mqResponsesPath = getMqResponsesPath(scenarioRootDirectory, step);
        if (Files.exists(mqResponsesPath)) {
            File[] files = mqResponsesPath.toFile().listFiles(File::isFile);
            if (files != null) {
                for (File file : files) {
                    String code = file.getName().split("\\.")[0];
                    step.getMqMockResponseList().stream()
                            .filter(item -> code.equals(item.getCode()))
                            .findAny()
                            .ifPresent(item -> item.setResponseBody(readFile(file.toString())));
                }
            }
        } else {
            //актуальная версия загрузки тела ответов
            step.getMqMockResponseList()
                    .stream()
                    .map(MqMock::getResponses)
                    .flatMap(List::stream)
                    .filter(response -> StringUtils.isNotEmpty(response.getResponseFile()))
                    .forEach(response -> {
                        File file = new File(scenarioRootDirectory, response.getResponseFile());
                        if (file.exists()) {
                            response.setResponseBody(readFile(file.toString()));
                        }
                    });
        }

        Path mqRequestsPath = getMqRequestsPath(scenarioRootDirectory, step);
        if (Files.exists(mqRequestsPath) && step.getExpectedMqRequestList() != null) {
            File[] files = mqRequestsPath.toFile().listFiles(File::isFile);
            if (files != null) {
                for (File file : files) {
                    String code = file.getName().split("\\.")[0];
                    step.getExpectedMqRequestList().stream()
                            .filter(item -> code.equals(item.getCode()))
                            .findAny()
                            .ifPresent(item -> item.setRequestBody(readFile(file.toString())));
                }
            }
        }

        /*
            Данные блоки необходимы для сохранения информации о sql и mq сообщений из старой версии модели
            TODO: удалить данные блоки после окончательного прекращения поддержки старого формата
         */
        if (StringUtils.isNotEmpty(step.getSql())) {
            SqlData sqlData = new SqlData();
            sqlData.setSql(step.getSql());
            step.setSql(null);
            if (StringUtils.isNotEmpty(step.getSqlSavedParameter())) {
                sqlData.setSqlSavedParameter(step.getSqlSavedParameter());
                step.setSqlSavedParameter(null);
            }
            step.getSqlDataList().add(sqlData);
        }

        if (StringUtils.isNotEmpty(step.getMqName()) ||
            StringUtils.isNotEmpty(step.getMqMessage()) ||
            CollectionUtils.isNotEmpty(step.getMqPropertyList())) {
            MqMessage message = new MqMessage();
            message.setMessage(step.getMqMessage());
            step.setMqMessage(null);
            message.setQueueName(step.getMqName());
            step.setMqName(null);
            message.setProperties(step.getMqPropertyList());
            step.setMqPropertyList(null);
            step.getMqMessages().add(message);
        }

        if (step.getMqMockResponseList() != null) {
            for (MqMock mock : step.getMqMockResponseList()) {
                if (StringUtils.isNotEmpty(mock.getDestinationQueueName()) ||
                    StringUtils.isNotEmpty(mock.getResponseBody())) {
                    MqMockResponse response = new MqMockResponse();
                    response.setResponseBody(mock.getResponseBody());
                    mock.setResponseBody(null);
                    response.setDestinationQueueName(mock.getDestinationQueueName());
                    mock.setDestinationQueueName(null);
                    mock.getResponses().add(response);
                }
            }
        }
    }

    protected String scenarioPath(Scenario scenario) {
        String result = "";
        if (scenario != null) {
            if (scenario.getScenarioGroup() != null) {
                result += scenario.getScenarioGroup() + "/";
            }
            if (scenario.getCode() == null) {
                // TODO Проверять, существует ли такая директория
                scenario.setCode(translator.translate(scenario.getName()));
            }
            result += scenario.getCode() + "/";
        } else {
            result = "0/";
        }
        return result;
    }

    private void saveStepToFiles(int order, Step step, File scenarioRootDirectory) throws IOException {
        step.setCode(generateCodeForStep(order, step));
        if (step.getRequest() != null) {
            try {
                step.setRequestFile(stepPath(step) + REQUEST_JSON);
                File file = new File(scenarioRootDirectory + "/" + step.getRequestFile());
                FileUtils.writeStringToFile(file, step.getRequest(), FILE_ENCODING);
                step.setRequest(null);
            } catch (IOException e) {
                log.error("Save file " + scenarioRootDirectory + "/" + step.getRequestFile(), e);
            }
        } else {
            step.setRequestFile(null);
        }

        if (step.getExpectedResponse() != null) {
            try {
                step.setExpectedResponseFile(stepPath(step) + stepExpectedResponseFile(step));
                File file = new File(scenarioRootDirectory + "/" + step.getExpectedResponseFile());
                FileUtils.writeStringToFile(file, step.getExpectedResponse(), FILE_ENCODING);
                step.setExpectedResponse(null);
            } catch (IOException e) {
                log.error("Save file " + scenarioRootDirectory + "/" + step.getExpectedResponseFile(), e);
            }
        } else {
            step.setExpectedResponseFile(null);
        }

        if (step.getMqMessages() != null) {
            step.getMqMessages().forEach(message -> {
                if (message.getMessage() != null) {
                    try {
                        message.setMessageFile(stepPath(step) + stepMqMessageFile(message));
                        File file = new File(scenarioRootDirectory + File.separator + message.getMessageFile());
                        FileUtils.writeStringToFile(file, message.getMessage(), FILE_ENCODING);
                        message.setMessage(null);
                    } catch (IOException e) {
                        log.error("Save file {}", scenarioRootDirectory + File.separator + message.getMessageFile(), e);
                    }
                } else {
                    message.setMessageFile(null);
                }
            });
        }

        step.getMockServiceResponseList().forEach(mockServiceResponse -> {
            if (mockServiceResponse.getResponseBody() != null) {
                try {
                    mockServiceResponse.setResponseBodyFile(stepPath(step) + mockResponseBodyFile(mockServiceResponse));
                    File file = new File(scenarioRootDirectory + "/" + mockServiceResponse.getResponseBodyFile());
                    FileUtils.writeStringToFile(file, mockServiceResponse.getResponseBody(), FILE_ENCODING);
                    mockServiceResponse.setResponseBody(null);
                } catch (IOException e) {
                    log.error("Save file " + scenarioRootDirectory + "/" + mockServiceResponse.getResponseBodyFile(), e);
                }
            } else {
                mockServiceResponse.setResponseBodyFile(null);
            }
        });

        step.getExpectedServiceRequests().forEach(expectedServiceRequest -> {
            if (expectedServiceRequest.getExpectedServiceRequest() != null) {
                try {
                    expectedServiceRequest.setExpectedServiceRequestFile(stepPath(step) + expectedServiceRequestFile(expectedServiceRequest));
                    File file = new File(scenarioRootDirectory + "/" + expectedServiceRequest.getExpectedServiceRequestFile());
                    FileUtils.writeStringToFile(
                            file,
                            expectedServiceRequest.getExpectedServiceRequest(),
                            FILE_ENCODING
                    );
                    expectedServiceRequest.setExpectedServiceRequest(null);
                } catch (IOException e) {
                    log.error("Save file " + scenarioRootDirectory + "/" + expectedServiceRequest.getExpectedServiceRequestFile(), e);
                }
            } else {
                expectedServiceRequest.setExpectedServiceRequest(null);
            }
        });

        step.getMqMockResponseList().stream().map(MqMock::getResponses).flatMap(List::stream).forEach(response -> {
            String body = response.getResponseBody();
            if (body != null) {
                response.setResponseFile(stepPath(step) + mqMockResponseFile(response));
                File file = new File(scenarioRootDirectory + File.separator + response.getResponseFile());
                try {
                    FileUtils.writeStringToFile(file, response.getResponseBody(), FILE_ENCODING);
                    response.setResponseBody(null);
                } catch (IOException e) {
                    log.error("Save file {}", file, e);
                }
            } else {
                response.setResponseFile(null);
            }
        });

        saveItemsToFiles(
                getMqRequestsPath(scenarioRootDirectory, step),
                step.getExpectedMqRequestList(),
                ExpectedMqRequest::getRequestBody,
                ExpectedMqRequest::setRequestBody
        );
    }

    private String generateCodeForStep(int order, Step step) {
        String result = String.valueOf(order);
        if (StringUtils.isNotEmpty(step.getStepComment())) {
            result += "-" + translator.translate(step.getStepComment());
        }
        return result;
    }

    private <T extends CodeAccessible> void saveItemsToFiles(
            Path dataPath,
            List<T> items,
            Function<T, String> getter,
            BiConsumer<T, String> setter
    ) throws IOException {
        if (items == null) {
            return;
        }
        for (T item : items) {
            String data = getter.apply(item);
            if (StringUtils.isEmpty(data)) {
                continue;
            }
            if (StringUtils.isEmpty(item.getCode())) {
                item.generateCode();
            }
            setter.accept(item, null);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            File file = Paths.get(dataPath.toString(), item.getCode() + "." + FileExtensionsUtils.extensionByContent(data)).toFile();
            FileUtils.writeStringToFile(file, data, FILE_ENCODING);
        }
    }

    private Path getMqRequestsPath(File scenarioRootDirectory, Step step) {
        return Paths.get(scenarioRootDirectory.toString(), stepPath(step), "mq-requests");
    }

    @Deprecated
    private Path getMqResponsesPath(File scenarioRootDirectory, Step step) {
        return Paths.get(scenarioRootDirectory.toString(), stepPath(step), "mq-responses");
    }

    private String readFile(String path) {
        try {
            File file = new File(path);
            return FileUtils.readFileToString(file, FILE_ENCODING);
        } catch (IOException e) {
            log.error("Reading file {}", path, e);
        }
        return null;
    }

    private String stepPath(Step step) {
        return "steps/" + step.getCode() + "/";
    }

    private String stepExpectedResponseFile(Step step) {
        return "expected-response." + FileExtensionsUtils.extensionByContent(step.getExpectedResponse());
    }

    private String stepMqMessageFile(MqMessage message) {
        return String.format(
                "mq-messages%s%s.%s",
                File.separator,
                UUID.randomUUID().toString(),
                FileExtensionsUtils.extensionByContent(message.getMessage())
        );
    }

    private String mockResponseBodyFile(MockServiceResponse mockServiceResponse) {
        if (mockServiceResponse.getCode() == null) {
            mockServiceResponse.setCode(UUID.randomUUID().toString());
        }
        return "mock-response-" + mockServiceResponse.getCode() + "." +
               FileExtensionsUtils.extensionByContent(mockServiceResponse.getResponseBody());
    }

    private String expectedServiceRequestFile(ExpectedServiceRequest expectedServiceRequest) {
        if (expectedServiceRequest.getCode() == null) {
            expectedServiceRequest.setCode(UUID.randomUUID().toString());
        }
        return "expected-service-request-" + expectedServiceRequest.getCode() + "." +
               FileExtensionsUtils.extensionByContent(expectedServiceRequest.getExpectedServiceRequest());
    }

    private String mqMockResponseFile(MqMockResponse response) {
        return String.format(
                "%s/%s.%s",
                MQ_MOCK_RESPONSES_PATH,
                UUID.randomUUID().toString(),
                FileExtensionsUtils.extensionByContent(response.getResponseBody())
        );
    }

    protected Scenario loadScenarioFromFiles(File scenarioDirectory, String group, boolean fetchSteps) throws IOException {
        File scenarioFile = new File(scenarioDirectory, SCENARIO_YML_FILENAME);
        if (scenarioFile.exists()) {
            Scenario scenario = YamlUtils.loadAs(scenarioFile, Scenario.class);
            scenario.setCode(scenarioFile.getParentFile().getName());
            scenario.setScenarioGroup(group);
            File scenarioRootDirectory = scenarioFile.getParentFile();
            if (fetchSteps) {
                scenario.getStepList().forEach(step -> loadStepFromFiles(step, scenarioRootDirectory));
            } else {
                scenario.getStepList().clear();
            }
            return scenario;
        } else {
            Scenario scenario = new Scenario();
            scenario.setScenarioGroup(group);
            return  scenario;
        }

    }
}
