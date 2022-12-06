package ramana.example.niotcpserver.util;

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
}
