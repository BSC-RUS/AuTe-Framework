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

package ru.bsc.test.at.executor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by smakarov
 * 20.04.2018 12:10
 */
@Getter
@Setter
public class MqMessage implements Serializable, AbstractModel {
    private static final long serialVersionUID = -2284237307005166339L;

    private String queueName;
    private String message;
    private String messageFile;
    private List<NameValueProperty> properties = new LinkedList<>();

    public MqMessage copy() {
        MqMessage copy = new MqMessage();
        copy.setQueueName(getQueueName());
        copy.setMessage(getMessage());
        if (getProperties() != null) {
            copy.setProperties(new LinkedList<>());
            properties.forEach(p -> copy.getProperties().add(p.copy()));
        }
        return copy;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return StringUtils.isEmpty(this.queueName) || StringUtils.isEmpty(this.message);
    }
}
