package ramana.example.niotcpserver.util;

import ramana.example.niotcpserver.codec.http.request.Header;
import ramana.example.niotcpserver.codec.http.request.RequestMessage;
import ramana.example.niotcpserver.types.LinkedList;

import java.lang.reflect.Field;

public class TestUtil {
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
        System.out.println("Method: " + requestMessage.method + " Path: " + requestMessage.path);
        System.out.println("queryParameters: " + requestMessage.queryParameters);
        System.out.println("Headers: ");
        for (Header header: requestMessage.headers) {
            System.out.println(header.name + ": " + header.values);
        }
        System.out.println();
    }
}
