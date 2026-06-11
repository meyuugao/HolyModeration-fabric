package me.yuugao.holymoderation.client.di;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.PostConstruct;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DIContainer {
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();

    private final ThreadLocal<List<String>> resolutionStack = ThreadLocal.withInitial(ArrayList::new);

    private final int MAX_DEPTH = 30;

    private static @NotNull Constructor<?> getConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> selected = null;

        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                selected = c;
                break;
            }
        }

        if (selected == null && constructors.length > 0) {
            selected = constructors[0];
        }

        if (selected == null) {
            throw new RuntimeException("Исключение в DIContainer/getConstructor: Не найден конструктор для " + clazz.getName());
        }
        return selected;
    }

    public <T> void register(Class<T> type, Class<? extends T> impl) {
        implementations.put(type, impl);
    }

    public <T> void registerInstance(Class<T> type, T instance) {
        singletons.put(type, instance);
    }

    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        List<String> stack = resolutionStack.get();
        stack.add("get(" + type.getSimpleName() + ")");

        if (stack.size() > MAX_DEPTH) {
            String chain = String.join(" -> ", stack);
            resolutionStack.remove();
            throw new RuntimeException(
                    "Превышена глубина разрешения DI! Возможна циклическая зависимость.\n" +
                            "Цепочка: " + chain
            );
        }

        try {
            Object existing = singletons.get(type);
            if (existing != null) {
                return (T) existing;
            }

            Supplier<?> factory = factories.get(type);
            if (factory != null) {
                T instance = (T) factory.get();
                if (type.isAnnotationPresent(Singleton.class)) {
                    singletons.put(type, instance);
                }
                return instance;
            }

            Class<?> implClass = implementations.get(type);
            if (implClass == null) {
                implClass = type;
            }

            T instance = createInstance(implClass);

            if (type.isAnnotationPresent(Singleton.class) || implClass.isAnnotationPresent(Singleton.class)) {
                singletons.put(type, instance);
            }

            return instance;
        } finally {
            if (!stack.isEmpty()) {
                stack.remove(stack.size() - 1);
            }
            if (stack.isEmpty()) {
                resolutionStack.remove();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<?> clazz) {
        List<String> stack = resolutionStack.get();
        stack.add("create(" + clazz.getSimpleName() + ")");

        if (stack.size() > MAX_DEPTH) {
            String chain = String.join(" -> ", stack);
            resolutionStack.remove();
            throw new RuntimeException(
                    "Превышена глубина разрешения DI! Возможна циклическая зависимость.\n" +
                            "Цепочка: " + chain
            );
        }

        try {
            Constructor<?> selected = getConstructor(clazz);

            Parameter[] params = selected.getParameters();
            Object[] args = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                args[i] = get(params[i].getType());
            }

            selected.setAccessible(true);
            T instance = (T) selected.newInstance(args);

            injectFields(instance);
            invokePostConstruct(instance);

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Исключение в DIContainer/createInstance: " + clazz.getName(), e);
        } finally {
            if (!stack.isEmpty()) {
                stack.remove(stack.size() - 1);
            }
            if (stack.isEmpty()) {
                resolutionStack.remove();
            }
        }
    }

    public void injectFields(Object target) {
        Class<?> clazz = target.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    field.set(target, get(field.getType()));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Исключение в DIContainer/injectFields:" + field.getName(), e);
                }
            }
        }
    }

    private void invokePostConstruct(Object instance) {
        for (java.lang.reflect.Method method : instance.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (method.getParameterCount() != 0) {
                    throw new RuntimeException("Исключение в DIContainer/invokePostConstruct: @PostConstruct метод не должен иметь параметров: " + method);
                }
                method.setAccessible(true);
                try {
                    method.invoke(instance);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Исключение в DIContainer/invokePostConstruct: " + method, e);
                }
            }
        }
    }
}