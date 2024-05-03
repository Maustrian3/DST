package dst.ass2.ioc.di.impl;

import dst.ass2.ioc.di.*;
import dst.ass2.ioc.di.annotation.Component;
import dst.ass2.ioc.di.annotation.Initialize;
import dst.ass2.ioc.di.annotation.Inject;
import dst.ass2.ioc.di.annotation.Property;
import dst.ass2.ioc.di.annotation.Scope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ObjectContainer implements IObjectContainer {

    private Properties properties = new Properties();
    private HashMap<Class<?>, Object> singletons = new HashMap<>();

    public ObjectContainer(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    // TODO catch all checked java exception and throw InjectionException
    @Override
    public <T> T getObject(Class<T> type) throws InjectionException {
        if(!type.isAnnotationPresent(Component.class)) {
            throw new InvalidDeclarationException("Type is not a component");
        }

        // Check if object is already created
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }

        // Create new object
        T object = null;
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();

            // Make the constructor accessible even if it is private
            constructor.setAccessible(true);

            object = constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new ObjectCreationException(e);
        }


        // Get and set fields/methods
        ArrayList<Field> fields = new ArrayList<>(List.of(type.getDeclaredFields()));
        ArrayList<Method> methods = new ArrayList<>(List.of(type.getDeclaredMethods()));

        // Recursively get all fields and methods from superclasses
        Class<?> superType = type;
        while (superType.getSuperclass() != null) {
            superType = superType.getSuperclass();
            fields.addAll(Arrays.asList(superType.getDeclaredFields()));
            methods.addAll(Arrays.asList(superType.getDeclaredMethods()));
        }


        // Inject fields
        for (Field field : fields) {
            Inject fieldAnnotation = field.getAnnotation(Inject.class);
            if (fieldAnnotation == null) {
                continue;
            }

            // Make the field accessible even if it is private
            field.setAccessible(true);

            Class<?> targetType = fieldAnnotation.targetType();
            if(targetType == Void.class) {
                targetType = field.getType();
            }

            try {
                field.set(object, getObject(targetType));
            } catch (IllegalAccessException e) {
                if (!fieldAnnotation.optional()) {
                    throw new InjectionException(e);
                }
            } catch (IllegalArgumentException e) {
                throw new InvalidDeclarationException(e);
            }
        }

        Properties properties = (Properties) this.properties.clone();

        // Set field values
        for (Field field : fields) {
            Property fieldAnnotation = field.getAnnotation(Property.class);
            if (fieldAnnotation == null) {
                continue;
            }

            // Make the field accessible even if it is private
            field.setAccessible(true);

            String key = fieldAnnotation.value();
            if (key == null) {
                throw new ObjectCreationException("Property key is null");
            }

            String propertyValue = properties.getProperty(key);
            if (propertyValue == null) {
                throw new ObjectCreationException("Property value is null");
            }

            Class<?> fieldType = field.getType();
            Object fieldValue = null;

            try {
                if(fieldType.equals(String.class)) {
                    fieldValue = propertyValue;
                } else if(fieldType.isPrimitive()) {
                    if (fieldType.equals(int.class)) {
                        fieldValue = Integer.parseInt(propertyValue);
                    } else if (fieldType.equals(boolean.class)) {
                        fieldValue = Boolean.parseBoolean(propertyValue);
                    } else if (fieldType.equals(double.class)) {
                        fieldValue = Double.parseDouble(propertyValue);
                    } else if (fieldType.equals(float.class)) {
                        fieldValue = Float.parseFloat(propertyValue);
                    } else if (fieldType.equals(long.class)) {
                        fieldValue = Long.parseLong(propertyValue);
                    } else {
                        throw new TypeConversionException("Unsupported primitive type");
                    }
                } else {
                    Method valueOfMethod = fieldType.getDeclaredMethod("valueOf", String.class);
                    // Invoke the static (-> null) method valueOf with the property value as argument
                    fieldValue = valueOfMethod.invoke(null, propertyValue);
                }

                field.set(object, fieldValue);

            } catch (NumberFormatException | InvocationTargetException e) {
                throw new TypeConversionException(e);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // Invoke initialize method
        for(Method method : methods) {
            if(!method.isAnnotationPresent(Initialize.class)) {
                continue;
            }

            // Make the method accessible even if it is private
            method.setAccessible(true);

            if(method.getParameterCount() != 0) {
                throw new InvalidDeclarationException("Initialize methods should not have parameters");
            }

            try {
                method.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ObjectCreationException(e);
            }
        }

        if(type.getAnnotation(Component.class).scope() == Scope.SINGLETON){
            singletons.put(type, object);
        }

        return object;
    }
}
