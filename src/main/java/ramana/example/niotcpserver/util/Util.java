package ramana.example.niotcpserver.util;

import ramana.example.niotcpserver.handler.ChannelHandler;
import ramana.example.niotcpserver.io.Allocator;
import ramana.example.niotcpserver.types.InternalException;
import ramana.example.niotcpserver.types.LinkedList;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class Util {
    public static String classPathToNormalizedPath(String path) {
        return Util.class.getResource(path).getPath().substring(1).replaceAll("%20", " ");
    }

    public static <T> LinkedList<T> createLinkedList(List<Class<? extends T>> classList) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        LinkedList<T> list = new LinkedList<>();
        for (Class<? extends T> clazz: classList) {
            list.add(clazz.getConstructor((Class<?>[]) null).newInstance((Object[]) null));
        }
        return list;
    }

    public static <T> LinkedList<T> createLinkedListFromInstanceList(List<T> inputList) {
        LinkedList<T> list = new LinkedList<>();
        for(T instance: inputList) {
            list.add(instance);
        }
        return list;
    }

    public static String toString(Object obj) throws InternalException {
        if(obj == null) return null;
        Allocator.Resource<ByteBuffer> resource;
        if(obj instanceof Allocator.Resource) {
            resource = (Allocator.Resource<ByteBuffer>) obj;
            ByteBuffer byteBuffer = resource.get();
            byteBuffer = byteBuffer.duplicate();
            byteBuffer.flip();
            return Charset.defaultCharset().decode(byteBuffer).toString();
        }
        return obj.toString();
    }

    public static String normalizeClassName(String source) {
        if(source.length() < 30) return source;
        int i1 = source.lastIndexOf('.');
        if(i1 == -1) return source;
        String clazz = source.substring(i1 + 1);
        String packageName = source.substring(0, i1);
        StringBuilder builder = new StringBuilder();
        int i2 = -1;
        while ((i2 = packageName.indexOf('.')) != -1) {
            builder.append(packageName.charAt(0) + ".");
            packageName = packageName.substring(i2 + 1);
        }
        builder.append(packageName.charAt(0) + ".");
        return builder.toString() + clazz;
    }

    public static String normalizeClassName(List<Class<? extends ChannelHandler>> channelHandlers) {
        StringBuilder builder = new StringBuilder();
        Iterator<Class<? extends ChannelHandler>> iterator = channelHandlers.iterator();
        builder.append("[");
        while (iterator.hasNext()) {
            builder.append(normalizeClassName(iterator.next().getName()));
            if(iterator.hasNext()) builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}
