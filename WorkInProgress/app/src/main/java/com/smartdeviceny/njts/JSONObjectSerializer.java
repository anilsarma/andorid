package com.smartdeviceny.njts;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JSONObjectSerializer {

    static public JSONObject marshall(Object object) throws Exception {
        JSONObject obj = new JSONObject();

        Field fields[] = object.getClass().getDeclaredFields();
        for (Field fld : fields) {
            if (fld.isAnnotationPresent(Persist.class)) {
                fld.setAccessible(true);
                Persist annotation = (Persist) fld.getAnnotation(Persist.class);
                if (annotation.state() == Persist.State.YES) {
                    if (primitiveType(fld.getType())) {
                        try {
                            obj.put(fld.getName(), fld.get(object));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (fld.getType().isArray()) {
                        try {
                            Array values = (Array) fld.get(object);
                            Array.getLength(values);
                            JSONArray a = new JSONArray();
                            for (int i = 0; i < Array.getLength(values); i++) {
                                System.out.println(Array.get(object, i));
                                a.put(i, marshall(Array.get(object, i)));
                            }
                            obj.put(fld.getName(), a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (isSpecial(fld.getType())) {
                        try {
                            Object value = fld.get(object);
                            if (value != null) {
                                obj.put(fld.getName(), _marshall(value));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            obj.put(fld.getName(), marshall(fld.get(object)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("persistence " + fld.getName() + " " + annotation.state());
            }
        }
        return obj;
    }

    static public Object unmarshall(Class<?> type, JSONObject jsonObject) throws Exception {
        Constructor<?> constructor = type.getConstructor();
        constructor.setAccessible(true);
        Object object = constructor.newInstance(); //(new Class[]{}).newInstance();

        Field fields[] = object.getClass().getDeclaredFields();
        for (Field fld : fields) {
            if (fld.isAnnotationPresent(Persist.class)) {
                fld.setAccessible(true);
                if (!jsonObject.has(fld.getName())) {
                    continue;
                }
                Persist annotation = (Persist) fld.getAnnotation(Persist.class);
                if (annotation.state() == Persist.State.YES) {
                    if (primitiveType(fld.getType())) {
                        try {
                            fld.set(object, jsonObject.get(fld.getName()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (fld.getType().isArray()) {
                        try {
                            Array values = (Array) fld.get(object);
                            Array.getLength(values);
                            JSONArray a = jsonObject.getJSONArray(fld.getName());
                            for (int i = 0; i < a.length(); i++) {
                                Array.set(values, i, a.get(i));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (isSpecial(fld.getType())) {
                        try {
                            fld.set(object, _unmarshall(fld, jsonObject.get(fld.getName())));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            fld.set(object, unmarshall(fld.getType(), jsonObject.getJSONObject(fld.getName())));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                System.out.println("persistence " + fld.getName() + " " + annotation.state());
            }
        }
        return object;
    }

    private static boolean primitiveType(Class<?> type) {
        if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            return true;
        }
        if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            return true;
        }
        if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            return true;
        }
        if (type.isAssignableFrom(String.class)) {
            return true;
        }
        if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            return true;
        }
        return false;
    }

    private static boolean isSpecial(Class<?> type) {
        if (type.isAssignableFrom(Date.class)) {
            return true;
        }
        if (type.isAssignableFrom(ArrayList.class)) {
            return true;
        }
        return false;
    }

    private static Object _marshall(Object object) throws Exception {
        if (object.getClass().isAssignableFrom(Date.class)) {
            return ((Date) object).getTime();
        }
        if (object.getClass().isAssignableFrom(ArrayList.class)) {
            JSONArray array = new JSONArray();
            int i = 0;
            for (Object tmp : (List) object) {
                if (isSpecial(tmp.getClass())) {
                    array.put(i++, tmp);
                }
                if (primitiveType(tmp.getClass())) {
                    array.put(i++, tmp);
                } else {
                    array.put(i++, marshall(tmp));
                }
            }
            return array;
        }

        return object;
    }

    private static Object _unmarshall(Class cls, Object object) throws Exception {
        if (cls.isAssignableFrom(Date.class)) {
            return new Date((Long) object);
        }
        return object;
    }

    private static Object _unmarshall(Field fld, Object object) throws Exception {
        if (fld.getType().isAssignableFrom(Date.class)) {
            return _unmarshall(fld.getType(), object);
        }
        if (fld.getType().isAssignableFrom(ArrayList.class)) {
            JSONArray array = (JSONArray) object;
            ArrayList data = new ArrayList();
            for (int i = 0; i < array.length(); i++) {
                Object obj = array.get(i);
                if (fld.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType gtype = (ParameterizedType) fld.getGenericType();
                    Class ptype = Class.forName(gtype.getActualTypeArguments()[0].getTypeName());
                    System.out.println(gtype);
                    try {
                        if (primitiveType(ptype)) {
                            data.add(array.get(i));
                        } else if (isSpecial(ptype)) {
                            data.add(_unmarshall(ptype, array.get(i)));
                        } else {
                            data.add(unmarshall(ptype, (JSONObject) array.get(i)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    if (primitiveType(tmp.getClass())) {
//                        array.put(i++, tmp);
//                    } else {
//                        array.put(i++, marshall(tmp));
//                    }
                }
            }
            return data;
        }
        return object;
    }


    public static String stringify(Object obj) {
        StringBuffer str = new StringBuffer();
        str.append("[" + obj.getClass().getName() + " ");
        Field fields[] = obj.getClass().getDeclaredFields();
        for (Field fld : fields) {
            fld.setAccessible(true);
            if (fld.isAnnotationPresent(Persist.class)) {
                Persist ann = fld.getAnnotation(Persist.class);
                if (ann.state() == Persist.State.YES) {
                    try {
                        str.append(fld.getName() + "=" + fld.get(obj) + ", ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        str.append("]");
        return str.toString();
    }
}
