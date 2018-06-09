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

package ru.bsc.velocity.directive;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.Writer;

/**
 * Created by sdoroshin on 14.08.2017.
 *
 */
public class GroovyDirective extends Directive {
    @Override
    public String getName() {
        return "groovy";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        String groovyScript = node.jjtGetChild(0).getFirstToken().image;

        Binding binding = new Binding();
        binding.setVariable("context", context);
        new GroovyShell(binding).evaluate(groovyScript);

        return true;
    }
}
