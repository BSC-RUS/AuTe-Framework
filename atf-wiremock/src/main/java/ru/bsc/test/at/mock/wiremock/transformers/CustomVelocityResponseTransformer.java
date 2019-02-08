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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.tools.ToolManager;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.bsc.test.at.mock.filter.utils.MultipartToBase64ConverterServletRequest.DOUBLE_DASH;
import static ru.bsc.test.at.util.Constants.CONVERT_BASE64_IN_MULTIPART;

/**
 * Created by sdoroshin on 26.07.2017.
 *
 */
@Slf4j
public class CustomVelocityResponseTransformer extends ResponseDefinitionTransformer {

    private static final String NEW_LINE_VAR = "\r|\n|(\r\n)|(\n\n)|(\r\r)";

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
        byte[] binaryBody = null;

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
        if(isPresentConvertCommand(responseDefinition.getHeaders().getHeader(CONVERT_BASE64_IN_MULTIPART))) {
            log.info(" >>> ");
            log.info(body);
            binaryBody = parseConvertBody(body);
            return buildResponse(binaryBody, body, responseDefinition, true);
        }
        return buildResponse(binaryBody, body, responseDefinition, false);
    }

    private ResponseDefinition buildResponse(byte[] binaryBody, String body, ResponseDefinition responseDefinition, boolean useBinaryIfNeedConvert) {
        if (useBinaryIfNeedConvert && binaryBody != null) {
            return ResponseDefinitionBuilder.like(responseDefinition).but()
                    .withBody(binaryBody)
                    .build();
        } else {
            return ResponseDefinitionBuilder.like(responseDefinition).but()
                    .withBody(body)
                    .build();
        }
    }

    private boolean isPresentConvertCommand(HttpHeader header) {
        return header != null && header.isPresent() && "true".equals(header.firstValue()) ;
    }

    private byte[] parseConvertBody(String body) {
        byte[] result = null;
        // если начинается с boundary
        if(body.startsWith(DOUBLE_DASH)) {
            // получаем части multipart'a
            String[] partsMultipart = body.split(NEW_LINE_VAR);
            List<Byte> buffer = new ArrayList<>();
            // обрабатываем каждую часть multipart'a - ищем base64
            for(String part : partsMultipart) {
                if(isNotBoundaryAndInBase64(part)) {
                    result = Base64.decode(part.getBytes());
                } else {
                    result = part.getBytes();
                }
                buffer.addAll(Arrays.asList(ArrayUtils.toObject(result)));
            }
            result = ArrayUtils.toPrimitive((Byte[]) buffer.toArray());
        } else {
            // если не начинается с boundary - проверяем, не base64 лежит в корне
            if(org.apache.commons.codec.binary.Base64.isArrayByteBase64(body.getBytes())) {
                result = Base64.decode(body.getBytes());
            } else {
                result = body.getBytes();
            }
        }
        return result;
    }

    private boolean isNotBoundaryAndInBase64(String part) {
        return !part.startsWith(DOUBLE_DASH) && org.apache.commons.codec.binary.Base64.isArrayByteBase64(part.getBytes());
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
