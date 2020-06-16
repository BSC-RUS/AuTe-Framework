/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

package ru.bsc.test.at.mock.mq;

import org.apache.velocity.context.Context;
import ru.bsc.test.at.mock.exception.UnexpectedMessageTypeException;
import ru.bsc.test.at.mock.mq.utils.MessageUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static ru.bsc.test.at.mock.mq.utils.MessageUtils.extractBodyFromByte;

/**
 * Created by smakarov
 * 05.07.2019 13:00
 */
public class JmsMessageHeadersExtractor {
    private static final String BODY_KEY = "body";
    private static final String HEADERS_KEY = "headers";
    private static final String HEADERS_IN_KEY = "in";
    private static final String HEADERS_OUT_KEY = "out";

    public Map<String, Object> createContext(Message message) throws JMSException, UnexpectedMessageTypeException {
        Map<String, Object> context = new HashMap<>();
        Map<Object, Object> headers = new HashMap<>();
        headers.put(HEADERS_IN_KEY, Collections.unmodifiableMap(getMessageHeaders(message)));
        headers.put(HEADERS_OUT_KEY, new HashMap<>());
        context.put(HEADERS_KEY, headers);
        context.put(BODY_KEY, MessageUtils.extractMessageBody(message));
        return context;
    }

    public Map<String, Object> createContext(Map<String, Object> messageHeaders, byte[] body) {
        Map<String, Object> context = new HashMap<>();
        Map<Object, Object> headers = new HashMap<>();
        headers.put(HEADERS_IN_KEY, Collections.unmodifiableMap(messageHeaders));
        headers.put(HEADERS_OUT_KEY, new HashMap<>());
        context.put(HEADERS_KEY, headers);
        context.put(BODY_KEY, extractBodyFromByte(body));
        return context;
    }

    @SuppressWarnings("unchecked")
    public void setHeadersFromContext(Message message, Context context) throws JMSException {
        Map<String, Object> headers = (Map<String, Object>) context.get(HEADERS_KEY);
        if (headers == null) {
            return;
        }
        Map<String, Object> outHeaders = (Map<String, Object>) headers.get(HEADERS_OUT_KEY);
        if (outHeaders == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : outHeaders.entrySet()) {
            message.setObjectProperty(entry.getKey(), entry.getValue());
        }
    }

    private Map<Object, Object> getMessageHeaders(Message message) throws JMSException {
        Map<Object, Object> headers = new HashMap<>();
        Enumeration names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            headers.put(name, message.getObjectProperty(name));
        }
        return headers;
    }
}
