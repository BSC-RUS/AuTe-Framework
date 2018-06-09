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

package ru.bsc.velocity.transformer;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.tools.ToolManager;
import ru.bsc.velocity.directive.GroovyDirective;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class VelocityTransformer {

    private Context context;

    public String transform(String requestBody, Map<String, String> requestHeaders, String stringTemplate) {
        final VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();
        final ToolManager toolManager = new ToolManager();
        toolManager.setVelocityEngine(velocityEngine);
        context = toolManager.createContext();
        addBodyToContext(requestBody);
        addHeadersToContext(requestHeaders);

        String render;
        try {
            render = render(stringTemplate);
        } catch (ParseException e) {
            render = e.getMessage();
        }
        return render;
    }

    private String render(final String stringTemplate) throws ParseException {
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        runtimeServices.setProperty("userdirective", GroovyDirective.class.getName());
        StringReader reader = new StringReader(stringTemplate);
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

    private void addBodyToContext(final String body) {
        if (body != null && !body.isEmpty()) {
            context.put("requestBody", body);
        }
    }

    private void addHeadersToContext(final Map<String, String> headers) {
        if (headers != null) {
            headers.forEach((s, s2) -> context.put("requestHeader".concat(s), s2));
        }
    }
}
