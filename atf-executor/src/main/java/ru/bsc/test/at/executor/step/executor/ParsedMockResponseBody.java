package ru.bsc.test.at.executor.step.executor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by smakarov
 * 26.10.2018 14:21
 */
public class ParsedMockResponseBody {
    private static final String REGULAR_EXPRESSION = "(Thread\\.)?sleep\\((\\d+)\\)";
    private static final String START = "#groovy()";
    private static final String END = "#end";

    private final String body;
    private final Pattern pattern;

    public ParsedMockResponseBody(String body) {
        this.body = body;
        this.pattern = Pattern.compile(REGULAR_EXPRESSION);
    }

    public long groovyDelay() {
        try {
            return extractDelay();
        } catch (Exception e) {
            return 0L;
        }
    }

    private long extractDelay() {
        long delay = 0L;
        int start = body.indexOf(START);
        if (start != -1) {
            int end = body.indexOf(END, start);
            String script = body.substring(start + START.length(), end);
            Matcher matcher = pattern.matcher(script);
            while (matcher.find() && matcher.groupCount() > 0) {
                delay += Long.parseLong(matcher.group(2));
            }
        }
        return delay;
    }
}
