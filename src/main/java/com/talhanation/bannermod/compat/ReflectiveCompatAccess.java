package com.talhanation.bannermod.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class ReflectiveCompatAccess {

    @FunctionalInterface
    public interface ClassResolver {
        Class<?> resolve(String className) throws ClassNotFoundException;
    }

    private final ClassResolver classResolver;

    public ReflectiveCompatAccess() {
        this(Class::forName);
    }

    ReflectiveCompatAccess(ClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    public Optional<Class<?>> findClass(String className) {
        try {
            return Optional.of(classResolver.resolve(className));
        }
        catch (ClassNotFoundException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    public Optional<Method> findMethod(String className, String methodName, Class<?>... parameterTypes) {
        return findClass(className).flatMap(type -> findMethod(type, methodName, parameterTypes));
    }

    public Optional<Method> findMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
        try {
            return Optional.of(type.getMethod(methodName, parameterTypes));
        }
        catch (NoSuchMethodException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    public Optional<Field> findField(String className, String fieldName) {
        return findClass(className).flatMap(type -> findField(type, fieldName));
    }

    public Optional<Field> findField(Class<?> type, String fieldName) {
        try {
            return Optional.of(type.getField(fieldName));
        }
        catch (NoSuchFieldException | LinkageError ignored) {
            return Optional.empty();
        }
    }

    public Optional<Object> invoke(Method method, Object target, Object... args) {
        try {
            return Optional.ofNullable(method.invoke(target, args));
        }
        catch (ReflectiveOperationException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public Optional<Integer> getInt(Field field, Object target) {
        try {
            return Optional.of(field.getInt(target));
        }
        catch (IllegalAccessException | RuntimeException ignored) {
            return Optional.empty();
        }
    }

    public boolean setInt(Field field, Object target, int value) {
        try {
            field.setInt(target, value);
            return true;
        }
        catch (IllegalAccessException | RuntimeException ignored) {
            return false;
        }
    }
}
