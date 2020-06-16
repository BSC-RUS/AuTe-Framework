package ru.bsc.test.at.mock.mq.utils;

import ru.bsc.test.at.mock.exception.UnexpectedMessageTypeException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class MessageUtils {

    private MessageUtils() {
    }

    public static String extractMessageBody(Message message) throws JMSException, UnexpectedMessageTypeException {
        if (message instanceof TextMessage) {
            return extractTextMessageBody((TextMessage) message);
        } else if (message instanceof BytesMessage) {
            return extractByteMessageBody((BytesMessage) message);
        } else {
            throw new UnexpectedMessageTypeException("Type of Message does not match " + BytesMessage.class.getName() + " or " + TextMessage.class.getName());
        }
    }

    public static String extractTextMessageBody(TextMessage message) throws JMSException {
        return message.getText();
    }

    public static String extractByteMessageBody(BytesMessage message) throws JMSException {
        byte[] byteData = new byte[(int) message.getBodyLength()];
        message.readBytes(byteData);
        return extractBodyFromByte(byteData);
    }

    public static String extractBodyFromByte(byte[] byteData) {
        return new String(byteData).replaceAll("<\\?xml(.+?)\\?>", "").trim();
    }

}
