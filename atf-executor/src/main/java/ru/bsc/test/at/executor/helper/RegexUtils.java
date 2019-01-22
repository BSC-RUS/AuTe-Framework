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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

  /** Get value from string using regular expression.
   * Only one group allowed, if regex contains more than one group
   * it will return the last one.
   *
   * @param source Full string for searching
   * @param regex  Regular expression for searching
   * @return       String with found value. If nothing
   *               found return empty string.
   */
  public static String getValueByRegex(String source, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(source);
    String result = "";

    while (matcher.find()) result = matcher.group();

    return result;
  }
}
