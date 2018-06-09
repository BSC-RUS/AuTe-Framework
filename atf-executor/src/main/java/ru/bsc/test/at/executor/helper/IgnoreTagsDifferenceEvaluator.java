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

package ru.bsc.test.at.executor.helper;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;
import ru.bsc.test.at.executor.validation.MaskComparator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xmlunit.diff.ComparisonType.*;

/**
 * Created by rmalyshev date: 30.11.12
 *
 */
class IgnoreTagsDifferenceEvaluator implements DifferenceEvaluator {

    private static final String TAG_REGEX = "\\s*(\\w+)\\s*\\(\\s*(\\w+)\\s*=\\s*([\\w#]+)\\s*\\)";
    private static final Set<ComparisonType> COMPARISON_TYPES = Collections.unmodifiableSet(EnumSet.of(
            NAMESPACE_PREFIX,
            NAMESPACE_URI,
            NO_NAMESPACE_SCHEMA_LOCATION,
            SCHEMA_LOCATION
    ));

    private Set<String> ignoredTags = new HashSet<>();

    IgnoreTagsDifferenceEvaluator(Set<String> ignoredTags) {
        if (ignoredTags != null) {
            this.ignoredTags = ignoredTags;
        }
    }

    private boolean isXSIType(Node node) {
        return node.getNodeType() == Node.ATTRIBUTE_NODE &&
                node.getLocalName().compareTo("type") == 0 &&
                Objects.equals(node.getNamespaceURI(), "http://www.w3.org/2001/XMLSchema-instance");
    }

    private String getNameSpaceFromPrefix(Node node) {
        final int beginIndex = node.getNodeValue().indexOf(':');
        if (beginIndex == -1) {
            return "";
        }
        return node.lookupNamespaceURI(node.getNodeValue().substring(0, beginIndex));
    }

    private String getNameWithoutPrefix(Node controlNode) {
        final int beginIndex = controlNode.getNodeValue().indexOf(':');
        if (beginIndex == -1) {
            return controlNode.getNodeValue();
        }
        return controlNode.getNodeValue().substring(beginIndex);
    }

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) {
            return outcome;
        }
        if (outcome == ComparisonResult.DIFFERENT && checkToDifferent(comparison)) {
            return ComparisonResult.EQUAL;
        }

        final Node controlNode = comparison.getControlDetails().getTarget();
        final Node testNode = comparison.getTestDetails().getTarget();

        if (comparison.getType() == ComparisonType.ATTR_VALUE && isXSIType(controlNode) && isXSIType(testNode)) {
            if (getNameSpaceFromPrefix(controlNode).compareTo(getNameSpaceFromPrefix(testNode)) != 0) {
                return ComparisonResult.DIFFERENT;
            }

            String withoutPrefixControl = getNameWithoutPrefix(controlNode);
            String withoutPrefixTest = getNameWithoutPrefix(testNode);
            if (withoutPrefixControl.compareTo(withoutPrefixTest) == 0) {
                return ComparisonResult.EQUAL;
            }
        }

        if(controlNode == null || ignoredTags == null) {
            return outcome;
        }

        if (checkControlNode(controlNode)) {
            return ComparisonResult.EQUAL;
        }

        // *ignore* check
        if (testNode != null && MaskComparator.compare(controlNode.getTextContent(), testNode.getTextContent())) {
            return ComparisonResult.EQUAL;
        }

        return outcome;
    }

    private boolean checkControlNode(Node controlNode) {
        Pattern pattern = Pattern.compile(TAG_REGEX);
        for (String ignoredTag : this.ignoredTags) {
            if (isControlNodeTag(ignoredTag, controlNode)) {
                return true;
            }
            Matcher matcher = pattern.matcher(ignoredTag);
            if(!matcher.find()) {
                continue;
            }

            String parent = matcher.group(1);
            String childKey = matcher.group(2);
            String childValue = matcher.group(3);
            NodeList childNodes = getNodeList(controlNode, parent, childKey);
            if (childNodes == null) {
                continue;
            }
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element) ||
                    !((Element) childNodes.item(i)).getTagName().equals(childKey)) {
                    continue;
                }

                Element childElement = (Element) childNodes.item(i);
                if (childElement.getTagName().equals(childKey) &&
                    childElement.getTextContent().equals(childValue)) {
                    return true;
                }
            }
        }
        return false;
    }

    private NodeList getNodeList(Node controlNode, String parent, String childKey) {
        if(!(controlNode.getParentNode() instanceof Element)) {
            return null;
        }

        Element controlElement = (Element) controlNode.getParentNode();
        if(!(controlElement.getParentNode() instanceof Element)) {
            return null;
        }

        Element parentElement = (Element) controlElement.getParentNode();
        if(!parentElement.getTagName().equals(parent) || !parentElement.hasChildNodes()) {
            return null;
        }

        NodeList childNodes = parentElement.getElementsByTagName(childKey);
        if(childNodes.getLength() <= 0) {
            return null;
        }
        return childNodes;
    }

    private boolean isControlNodeTag(String ignoredTag, Node controlNode) {
        if (!ignoredTag.matches(TAG_REGEX)) {
            if (controlNode.getParentNode() instanceof Element) {
                Element element = (Element) controlNode.getParentNode();
                return ignoredTag.equals(element.getTagName());
            }
        }
        return false;
    }

    private boolean checkToDifferent(Comparison comparison) {
        ComparisonType comparisonType = comparison.getType();
        if (ATTR_VALUE.equals(comparisonType) || ATTR_NAME_LOOKUP.equals(comparisonType)) {
            Attr target = (Attr) comparison.getControlDetails().getTarget();
            String parentNodeName = target.getOwnerElement().getLocalName();
            return ignoredTags.contains(parentNodeName);
        }

        return COMPARISON_TYPES.contains(comparisonType);
    }
}
