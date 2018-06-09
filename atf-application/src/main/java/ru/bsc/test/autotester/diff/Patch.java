/*
 * Diff Match and Patch
 * Copyright 2018 The diff-match-patch Authors.
 * https://github.com/google/diff-match-patch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.autotester.diff;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

/**
 * Class representing one patch operation.
 */
@SuppressWarnings("all")
public class Patch {
    public LinkedList<Diff> diffs;
    public int start1;
    public int start2;
    public int length1;
    public int length2;

    /**
     * Constructor.  Initializes with an empty list of diffs.
     */
    public Patch() {
        this.diffs = new LinkedList<Diff>();
    }

    /**
     * Emmulate GNU diff's format.
     * Header: @@ -382,8 +481,9 @@
     * Indicies are printed as 1-based, not 0-based.
     * @return The GNU diff string.
     */
    public String toString() {
        String coords1, coords2;
        if (this.length1 == 0) {
            coords1 = this.start1 + ",0";
        } else if (this.length1 == 1) {
            coords1 = Integer.toString(this.start1 + 1);
        } else {
            coords1 = (this.start1 + 1) + "," + this.length1;
        }
        if (this.length2 == 0) {
            coords2 = this.start2 + ",0";
        } else if (this.length2 == 1) {
            coords2 = Integer.toString(this.start2 + 1);
        } else {
            coords2 = (this.start2 + 1) + "," + this.length2;
        }
        StringBuilder text = new StringBuilder();
        text.append("@@ -").append(coords1).append(" +").append(coords2)
                .append(" @@\n");
        // Escape the body of the patch with %xx notation.
        for (Diff aDiff : this.diffs) {
            switch (aDiff.operation) {
                case INSERT:
                    text.append('+');
                    break;
                case DELETE:
                    text.append('-');
                    break;
                case EQUAL:
                    text.append(' ');
                    break;
            }
            try {
                text.append(URLEncoder.encode(aDiff.text, "UTF-8").replace('+', ' '))
                        .append("\n");
            } catch (UnsupportedEncodingException e) {
                // Not likely on modern system.
                throw new Error("This system does not support UTF-8.", e);
            }
        }
        return unescapeForEncodeUriCompatability(text.toString());
    }

    /**
     * Unescape selected chars for compatability with JavaScript's encodeURI.
     * In speed critical applications this could be dropped since the
     * receiving application will certainly decode these fine.
     * Note that this function is case-sensitive.  Thus "%3f" would not be
     * unescaped.  But this is ok because it is only called with the output of
     * URLEncoder.encode which returns uppercase hex.
     *
     * Example: "%3F" -> "?", "%24" -> "$", etc.
     *
     * @param str The string to escape.
     * @return The escaped string.
     */
    public String unescapeForEncodeUriCompatability(String str) {
        return str.replace("%21", "!").replace("%7E", "~")
                .replace("%27", "'").replace("%28", "(").replace("%29", ")")
                .replace("%3B", ";").replace("%2F", "/").replace("%3F", "?")
                .replace("%3A", ":").replace("%40", "@").replace("%26", "&")
                .replace("%3D", "=").replace("%2B", "+").replace("%24", "$")
                .replace("%2C", ",").replace("%23", "#");
    }

}
