/*
 * Copyright 2019 BSC Msc, LLC
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

package ru.bsc.test.at.executor.helper;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

public class XMLUtils {

    /**
     * Parse XML document and get value using XPath expression.
     *
     * @param source          original document
     * @param xpathExpression valid XPath expression for getting required value
     * @return string value by xpath expression
     */
    @SuppressWarnings("WeakerAccess")
    public static String getValueByXPath(String source, String xpathExpression) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(new InputSource(new StringReader(source)));
        XPath xPath = XPathFactory.newInstance().newXPath();
        return xPath.compile(xpathExpression).evaluate(xmlDocument);
    }
}
