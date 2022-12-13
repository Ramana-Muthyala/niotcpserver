package ramana.example.niotcpserver.util;

import ramana.example.niotcpserver.codec.http.request.Header;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.codec.parser.Util;
import ramana.example.niotcpserver.log.LogFactory;
import ramana.example.niotcpserver.types.LinkedList;

import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Logger;

public class TestUtil {
    private static final Logger logger = LogFactory.getLogger();
    public static Object invoke(Object obj, String field) throws NoSuchFieldException, IllegalAccessException {
        Field fieldObj = obj.getClass().getDeclaredField(field);
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
        for (Header header: requestMessage.headers) {
            logger.info(header.name + ": " + header.values);
        }
        logger.info("Content: " + ((requestMessage.body == null) ? null : new String(requestMessage.body)));
    }

    public static int getContentLength(List<Header> headers) {
        for (Header header: headers) {
            if(Util.REQ_HEADER_CONTENT_LENGTH.equals(header.name)) {
                return Integer.parseInt(header.values.get(0));
            }
        }
        return 0;
    }
}
