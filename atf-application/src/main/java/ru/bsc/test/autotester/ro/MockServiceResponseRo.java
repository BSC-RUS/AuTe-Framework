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

package ru.bsc.test.autotester.ro;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
@ApiModel(description = "Dynamic REST stub")
public class MockServiceResponseRo implements AbstractRo {
    private static final long serialVersionUID = -7918346254164488513L;

    @ApiModelProperty("Unique code")
    private String code;
    @ApiModelProperty("Relative URL of stub")
    private String serviceUrl;
    @ApiModelProperty("Shows that serviceName will be interpreted like RegExp")
    private Boolean urlPattern;
    @ApiModelProperty(value = "Method of request", allowableValues = "POST, GET, PUT, DELETE, PATCH")
    private String httpMethod;
    @ApiModelProperty("Text of response body")
    private String responseBody;
    @ApiModelProperty("Relative path to file with response text which stored on disk")
    private String responseBodyFile;
    @ApiModelProperty("Expected HTTP status of response")
    private Integer httpStatus;
    @ApiModelProperty("Content type of response like application/json or text/xml")
    private String contentType;
    @ApiModelProperty("Username for using basic authentication")
    private String userName;
    @ApiModelProperty("Password for using basic authentication")
    private String password;
    @ApiModelProperty(value = "Type of matching response", allowableValues = "empty, equalToJson, equalToXml, XPath, contains, matches, absent")
    private String typeMatching;
    @ApiModelProperty("Value which will be used to match response. Depends on typeMatching")
    private String pathFilter;
    @ApiModelProperty("Convert BASE64 to multipart")
    private Boolean convertBase64InMultipart;
    @ApiModelProperty("List of response HTTP headers")
    private List<HeaderItemRo> headers;
    @ApiModelProperty("Order of response for similar requests")
    private Integer responseOrder;

}
