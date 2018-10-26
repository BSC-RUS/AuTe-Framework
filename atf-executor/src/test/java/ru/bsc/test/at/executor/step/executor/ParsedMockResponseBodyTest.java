package ru.bsc.test.at.executor.step.executor;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by smakarov
 * 26.10.2018 16:11
 */
public class ParsedMockResponseBodyTest {
    private static final long DELAY = 10000L;
    private static final String GROOVY_THREAD_DELAY = "Thread.sleep(" + DELAY + ")\n";
    private static final String GROOVY_DELAY = "sleep(" + DELAY + ")\n";

    private static final String BODY_WITH_GROOVY_DELAY = "\n" +
            "#groovy()\n" +
            GROOVY_THREAD_DELAY +
            "#end\n" +
            "some body";

    private static final String BODY_WITH_TWO_GROOVY_DELAY = "\n" +
            "#groovy()\n" +
            GROOVY_THREAD_DELAY +
            GROOVY_THREAD_DELAY +
            "#end\n" +
            "some body";

    private static final String BODY_WITH_TWO_DIFFERENT_GROOVY_DELAY = "\n" +
            "#groovy()\n" +
            GROOVY_THREAD_DELAY +
            GROOVY_DELAY +
            "#end\n" +
            "some body";

    private static final String BODY_WITH_COMPLEX_GROOVY_SCRIPT_AND_TWO_DELAY = "\n" +
            "#groovy()\n" +
            "int a = 5\n" +
            GROOVY_THREAD_DELAY + "\n" +
            "int b = 6\n" +
            GROOVY_THREAD_DELAY + "\n" +
            "int c = a + b\n" +
            "#end\n" +
            "some body";

    private static final String BODY_WITH_INVALID_GROOVY_DELAY = "\n" +
            "#groovy()\n" +
            "Thread.sleep(not a number)\n" +
            "#end\n" +
            "some body";

    @Test
    public void extractWithoutScriptSuccess() {
        ParsedMockResponseBody body = new ParsedMockResponseBody("body without script");
        assertEquals(0, body.groovyDelay());
    }

    @Test
    public void extractOneDelayFromScript() {
        ParsedMockResponseBody body = new ParsedMockResponseBody(BODY_WITH_GROOVY_DELAY);
        assertEquals(DELAY, body.groovyDelay());
    }

    @Test
    public void extractTwoSameDelaysFromScript() {
        ParsedMockResponseBody body = new ParsedMockResponseBody(BODY_WITH_TWO_GROOVY_DELAY);
        assertEquals(DELAY + DELAY, body.groovyDelay());
    }

    @Test
    public void extractTwoDifferentDelaysFromScript() {
        ParsedMockResponseBody body = new ParsedMockResponseBody(BODY_WITH_TWO_DIFFERENT_GROOVY_DELAY);
        assertEquals(DELAY + DELAY, body.groovyDelay());
    }

    @Test
    public void extractTwoSameDelaysFromComplexScript() {
        ParsedMockResponseBody body = new ParsedMockResponseBody(BODY_WITH_COMPLEX_GROOVY_SCRIPT_AND_TWO_DELAY);
        assertEquals(DELAY + DELAY, body.groovyDelay());
    }

    @Test
    public void extractFromInvalidScript() {
        ParsedMockResponseBody body = new ParsedMockResponseBody(BODY_WITH_INVALID_GROOVY_DELAY);
        assertEquals(0, body.groovyDelay());
    }
}