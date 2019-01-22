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

@Getter
@Setter
@ApiModel(description = "REST request form-data item")
public class FormDataRo implements AbstractRo {
    private static final long serialVersionUID = -7730693222002977456L;

    @ApiModelProperty("Name of form field")
    private String fieldName;
    @ApiModelProperty(value = "Type of form field", allowableValues = "Text, File")
    private String fieldType;
    @ApiModelProperty("Value of text field. Available if fieldType = Text")
    private String value;
    @ApiModelProperty("Path to file relative to project directory. Available if fieldType = File")
    private String filePath;
    @ApiModelProperty("MIME-type of file set in filePath. Available if fieldType = File")
    private String mimeType;
}
