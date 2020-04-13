package ru.bsc.test.at.mock.mq.predicate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

@Slf4j
public class JmsMessagePredicateFactory {

    private static JmsMessagePredicateFactory factory;

    private JmsMessagePredicateFactory() {
    }

    public static JmsMessagePredicateFactory getInstance(){
        if (factory == null){
            factory = new JmsMessagePredicateFactory();
        }

        return factory;
    }

    public JmsMessagePredicate newJmsMessagePredicate(String source, String testId, String messageBody){
        Document xmlDocument = parseXml(messageBody);
        if (xmlDocument != null) {
            return new JmsXmlMessagePredicate(source, testId, xmlDocument);
        }

        DocumentContext jsonContext = parseJson(messageBody);
        if (jsonContext != null){
            return new JmsJsonMessagePredicate(source, testId, jsonContext);
        }

        return new JmsMessagePredicate(source, testId);
    }

    private DocumentContext parseJson(String json){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readValue(json, Object.class);
            return JsonPath.parse(json);
        }catch (IOException e){
            log.info("Cannot parse JSON: {}", e.getMessage());
            return null;
        }
    }

    private Document parseXml(String stringBody) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(stringBody)));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.info("Cannot parse XML document: {}", e.getMessage());
            return null;
        }
    }

}
