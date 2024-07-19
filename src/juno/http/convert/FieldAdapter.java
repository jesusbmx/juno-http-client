package juno.http.convert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldAdapter <T> {
    Field field;
    Method getMethod;

    public FieldAdapter(Field field, Method getMethod) {
        this.field = field;
        this.getMethod = getMethod;
    }

    public String getName() {
        return field.getName();
    }

    public T get(Object object) throws Exception {
        if (getMethod != null) 
            return (T) getMethod.invoke(object);
        else
            return (T) field.get(object);
    } 

    public static FieldAdapter[] fields(Class classOf) throws Exception {
        final Field[] fs = classOf.getDeclaredFields();
        final Method[] m = classOf.getDeclaredMethods();
        final FieldAdapter[] result = new FieldAdapter[fs.length];

        Method get;
        //Method set;
        int y = 0;

        for (Field f : fs) {
          get = findGetMethod(f, m);
          //set = OrmUtils.setMethod(f, m);
          result[y++] = new FieldAdapter(f, get);
        }

        final FieldAdapter[] copy = new FieldAdapter[y];
        System.arraycopy(result, 0, copy, 0, y);
        return copy;
    }

    public static String capitalize(String str) {
        char[] data = str.toCharArray();
        data[0] = Character.toUpperCase(data[0]);
        return new String(data);
    }

    public static Method findGetMethod(Field f, Method[] m) throws Exception {
        String name = capitalize(f.getName());
        String methodnameA = "get" + name;
        String methodnameB = "is" + name;

        Class[] parameterTypes;

        for (Method method : m) {
            parameterTypes = method.getParameterTypes();

            if (parameterTypes.length > 0) {
                continue;
            }

            if (method.getName().equals(methodnameA)) {
                return method;
            } else if (method.getName().equals(methodnameB)) {
                return method;
            }
        }

        return null;
    }

}
