package study.wyy.datatransfer.spring.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wyaoyao
 * @description
 * @date 2021/1/20 15:53
 */
public class ClassUtils {

    public static <T> Class<T> getGenericClassOnInterface(Class clz, Class interfaceClz, int index, Class defaultClz) {
        return getGenericClassOnInterface(clz, interfaceClz, index, defaultClz, new Type[]{});
    }

    public static <T> Class<T> getGenericClassOnInterface(Class clz, Class interfaceClz, int index, Class defaultClz, Type[] previousTypes) {
        if (clz == null) {
            return defaultClz;
        }
        TypeVariable[] typeParameters = clz.getTypeParameters();
        Map<String, Type> typeParameterMap = typeParameters.length > 0 ? new HashMap<>(typeParameters.length) : Collections.emptyMap();
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable typeVariable = typeParameters[i];
            typeParameterMap.put(typeVariable.getName(), previousTypes.length > i ? previousTypes[i] : null);
        }
        Type[] genericInterfaces = clz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) genericInterface;
                boolean matchInterface = interfaceClz == null || pType.getRawType().getTypeName().equalsIgnoreCase(interfaceClz.getTypeName());
                if (matchInterface && pType.getActualTypeArguments().length > index) {
                    Type type = pType.getActualTypeArguments()[index];
                    if (type instanceof ParameterizedType) {
                        return (Class<T>) ((ParameterizedType) type).getRawType();
                    }
                    Type previousType = typeParameterMap.get(type.getTypeName());
                    if (previousType instanceof ParameterizedType) {
                        return (Class<T>) ((ParameterizedType) previousType).getRawType();
                    }
                    if (type instanceof Class) {
                        return (Class<T>) type;
                    }
                    if (previousType instanceof Class) {
                        return (Class<T>) previousType;
                    }
                }
            }
        }
        Type genericSuperclass = clz.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            Type[] types = parameterizedType.getActualTypeArguments();
            return getGenericClassOnInterface(clz.getSuperclass(), interfaceClz, index, defaultClz, types);
        }
        return getGenericClassOnInterface(clz.getSuperclass(), interfaceClz, index, defaultClz, new Type[]{});
    }

    public static  <A extends Annotation> A getAnnotation(Object object, Class<A> annotationClass) {
        Class clazz = object.getClass();
        while (clazz != null) {
            //noinspection unchecked
            A annotation = (A) clazz.getAnnotation(annotationClass);
            if (null != annotation) {
                return annotation;
            } else {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

}
