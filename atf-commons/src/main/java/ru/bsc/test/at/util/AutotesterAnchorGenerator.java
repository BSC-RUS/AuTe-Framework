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

package ru.bsc.test.at.util;

import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.serializer.AnchorGenerator;

/**
 * Created by sdoroshin on 02.11.2017.
 *
 */
public class AutotesterAnchorGenerator implements AnchorGenerator {

    private long lastAnchorId;

    @Override
    public String nextAnchor(Node node) {
        if (node instanceof MappingNode) {
            NodeTuple idNode = ((MappingNode) node).getValue()
                    .stream()
                    .filter(nodeTuple -> nodeTuple.getKeyNode() instanceof ScalarNode)
                    .filter(nodeTuple -> "id".equals(((ScalarNode) nodeTuple.getKeyNode()).getValue()))
                    .findAny()
                    .orElse(null);
            if (idNode != null && idNode.getValueNode() instanceof ScalarNode) {
                String idValue = ((ScalarNode) idNode.getValueNode()).getValue();
                if (idValue != null) {
                    return "objId" + idValue;
                }
            }
        }
        return "id" + (lastAnchorId++);
    }
}
