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

package ru.bsc.test.at.mock.wiremock.transformers;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.tools.ToolManager;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sdoroshin on 26.07.2017.
 *
 */
public class CustomVelocityResponseTransformer extends ResponseDefinitionTransformer {
    /**
     * The Velocity context that will hold our request header
     * data.
     */
    private Context context;

    @Override
    public ResponseDefinition transform(final Request request,
                                        final ResponseDefinition responseDefinition, final FileSource files,
                                        final Parameters parameters) {
        final VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();
        final ToolManager toolManager = new ToolManager();
        toolManager.setVelocityEngine(velocityEngine);
        context = toolManager.createContext();
        addBodyToContext(request.getBodyAsString());
        addHeadersToContext(request.getHeaders());
        context.put("requestAbsoluteUrl", request.getAbsoluteUrl());
        context.put("requestUrl", request.getUrl());
        context.put("requestMethod", request.getMethod());
        String body;

        if (responseDefinition.specifiesBodyFile() && templateDeclared(responseDefinition)) {
            body = getRenderedBody(responseDefinition);
        } else if (responseDefinition.specifiesBodyContent()) {
            try {
                body = getRenderedBodyFromFile(responseDefinition);
            } catch (ParseException e) {
                body = e.getMessage();
                e.printStackTrace();
            }
        } else {
            return responseDefinition;
        }
        return ResponseDefinitionBuilder.like(responseDefinition).but()
                .withBody(body)
                .build();
    }

    private Boolean templateDeclared(final ResponseDefinition response) {
        Pattern extension = Pattern.compile(".vm$");
        Matcher matcher = extension.matcher(response.getBodyFileName());
        return matcher.find();
    }

    private void addHeadersToContext(final HttpHeaders headers) {
        for (HttpHeader header : headers.all()) {
            final String rawKey = header.key();
            final String transformedKey = rawKey.replaceAll("-", "");
            context.put("requestHeader".concat(transformedKey), header.values()
                    .toString());
        }
    }

    private void addBodyToContext(final String body) {
        if (body != null && !body.isEmpty()) {
            context.put("requestBody", body);
        }
    }

    private String getRenderedBodyFromFile(final ResponseDefinition response) throws ParseException {
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        StringReader reader = new StringReader(response.getBody());
        SimpleNode node = runtimeServices.parse(reader, "Template name");
        Template template = new Template();
        template.setEncoding("UTF-8");
        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.initDocument();

        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return String.valueOf(writer.getBuffer());
    }

    private String getRenderedBody(final ResponseDefinition response) {
        final Template template = Velocity.getTemplate(response.getBodyFileName(), "UTF-8");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return String.valueOf(writer.getBuffer());
    }

    @Override
    public String getName() {
        return "bsc-velocity-transformer";
    }

}
