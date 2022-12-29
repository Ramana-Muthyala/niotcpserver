package ramana.example.niotcpserver.util;

import ramana.example.niotcpserver.codec.http.Util;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.LinkedList;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class TestUtil {
    private static final Logger logger = LogFactory.getLogger();
    public static Object invoke(Object obj, String field) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field fieldObj = obj.getClass().getDeclaredField(field);
        fieldObj.setAccessible(true);
        return fieldObj.get(obj);
    }

    public static int size(LinkedList list) {
        int size = 0;
        if(list.head == null) return 0;
        size++;
        LinkedList.LinkedNode tmp = list.head;
        while((tmp = tmp.next) != null) size++;
        return size;
    }

    public static void print(RequestMessage requestMessage) {
        logger.info("Method: " + requestMessage.method + " Path: " + requestMessage.path);
        logger.info("queryParameters: " + requestMessage.queryParameters);
        logger.info("Headers: ");
        for (Map.Entry<String, ArrayList<String>> header: requestMessage.headers.entrySet()) {
            logger.info(header.getKey() + ": " + header.getValue());
        }
        logger.info("Content: " + ((requestMessage.body == null) ? null : new String(requestMessage.body)));
    }

    public static void print(ramana.example.niotcpserver.codec.http.request.v1.RequestMessage requestMessage) {
        logger.info("Method: " + requestMessage.method + " Path: " + requestMessage.path);
        logger.info("queryParameters: " + requestMessage.queryParameters);
        logger.info("Headers: ");
        if(requestMessage.headers != null) {
            for (Map.Entry<String, ArrayList<String>> header: requestMessage.headers.entrySet()) {
                logger.info(header.getKey() + ": " + header.getValue());
            }
        }
        logger.info("Content: " + ((requestMessage.body == null) ? null : new String(requestMessage.body)));
    }

    public static int getContentLength(Map<String, ArrayList<String>> headers) {
        if(headers.containsKey(Util.REQ_HEADER_CONTENT_LENGTH)) {
            return Integer.parseInt(headers.get(Util.REQ_HEADER_CONTENT_LENGTH).get(0));
        }
        return 0;
    }
}
