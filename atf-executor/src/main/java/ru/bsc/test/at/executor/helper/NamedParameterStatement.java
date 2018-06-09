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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by sdoroshin on 05.06.2017.
 *
 * @see <a href="http://www.javaworld.com/article/2077706/core-java/named-parameters-for-preparedstatement.html">Article</a>
 */
public class NamedParameterStatement implements AutoCloseable {
    private final PreparedStatement statement;
    private final Map<String, List<Integer>> indexMap;

    public NamedParameterStatement(Connection connection, String query) throws SQLException {
        indexMap = new HashMap<>();
        statement = connection.prepareStatement(parse(query, indexMap));
    }

    private static String parse(String query, Map<String, List<Integer>> paramMap) {
        int length = query.length();
        StringBuilder parsedQuery = new StringBuilder(length);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int index = 1;

        for(int i = 0; i < length; i++) {
            char c = query.charAt(i);
            if(inSingleQuote) {
                if(c == '\'') {
                    inSingleQuote = false;
                }
            } else if(inDoubleQuote) {
                if(c == '"') {
                    inDoubleQuote = false;
                }
            } else {
                if(c == '\'') {
                    inSingleQuote = true;
                } else if(c == '"') {
                    inDoubleQuote = true;
                } else if(c == ':' && i + 1 < length && Character.isJavaIdentifierStart(query.charAt(i + 1))) {
                    int j = i + 2;
                    while(j < length && Character.isJavaIdentifierPart(query.charAt(j))) {
                        j++;
                    }
                    String name = query.substring(i + 1, j);
                    // replace the parameter with a question mark
                    c = '?';
                    // skip past the end if the parameter
                    i += name.length();

                    List<Integer> indexList = paramMap.computeIfAbsent(name, k -> new LinkedList<>());
                    indexList.add(index);
                    index++;
                }
            }
            parsedQuery.append(c);
        }
        return parsedQuery.toString();
    }

    public void setString(String name, String value) throws SQLException {
        List<Integer> indexList = indexMap.get(name);
        if (indexList != null) {
            for (Integer index: indexList) {
                statement.setString(index, value);
            }
        }
    }

    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    @Override
    public void close() throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }
}
