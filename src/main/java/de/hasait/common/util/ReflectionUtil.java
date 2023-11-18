package de.hasait.common.util;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ReflectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);

    private ReflectionUtil() {
    }

    private static final Map<String, Annotation> annotationCache = new ConcurrentHashMap<>();

    public static <T> void forEachProperty(Class<T> beanClass, Function<PropertyDescriptor, Boolean> logic) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            LOG.warn("Introspection failed: {}", beanClass, e);
            return;
        }
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (!logic.apply(propertyDescriptor)) {
                break;
            }
        }
    }

    public static <T> boolean isRequired(Class<T> beanClass, PropertyDescriptor propertyDescriptor) {
        return findAnnotation(beanClass, propertyDescriptor, NotNull.class) != null;
    }

    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A findAnnotation(Class<?> beanType, PropertyDescriptor propertyDescriptor, Class<A> annotationClass) {
        String key = beanType.getName() + "|" + propertyDescriptor.getName() + "|" + annotationClass.getName();
        return (A) annotationCache.computeIfAbsent(key, ignored -> internalFindAnnotation(beanType, propertyDescriptor, annotationClass));
    }

    private static <A extends Annotation> A internalFindAnnotation(Class<?> beanType, PropertyDescriptor propertyDescriptor, Class<A> annotationClass) {
        Field field = getField(beanType, propertyDescriptor);
        if (field != null) {
            A annotation = AnnotationUtils.findAnnotation(field, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }

        Method readMethod = propertyDescriptor.getReadMethod();
        if (readMethod != null) {
            A annotation = AnnotationUtils.findAnnotation(readMethod, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }

        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (writeMethod != null) {
            A annotation = AnnotationUtils.findAnnotation(writeMethod, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    public static Field getField(Class<?> beanType, PropertyDescriptor propertyDescriptor) {
        try {
            return beanType.getField(propertyDescriptor.getName());
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

}
