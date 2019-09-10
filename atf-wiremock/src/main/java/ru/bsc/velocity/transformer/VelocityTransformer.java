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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@SuppressWarnings("Duplicates")
public class VelocityTransformer {

    @Getter
    private Context velocityContext;

    public String transform(String requestBody, Map<String, Object> context, String stringTemplate) {
        log.debug("transform (requestBody = {}, context = {}, template = {})", requestBody, context, stringTemplate);

        final VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();
        final ToolManager toolManager = new ToolManager();
        toolManager.setVelocityEngine(velocityEngine);
        velocityContext = toolManager.createContext();
        if (context != null) {
            context.forEach((k, v) -> velocityContext.put(k, v));
        }

        try {
            return render(stringTemplate);
        } catch (ParseException e) {
            log.error("exception while parsing, return exception message", e);
            return e.getMessage();
        }
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
        template.merge(velocityContext, writer);
        return String.valueOf(writer.getBuffer());
    }
}
