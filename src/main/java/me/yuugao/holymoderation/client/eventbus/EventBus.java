package me.yuugao.holymoderation.client.eventbus;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final Map<Class<?>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

    public void register(Object object) {
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && Event.class.isAssignableFrom(parameterTypes[0])) {
                    Class<?> eventType = parameterTypes[0];
                    Subscribe annotation = method.getAnnotation(Subscribe.class);
                    int priority = annotation.priority();
                    Subscriber subscriber = new Subscriber(object, method, priority);

                    subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(subscriber);
                    subscribers.get(eventType).sort((s1, s2) -> Integer.compare(s2.priority(), s1.priority()));
                }
            }
        }
    }

    public void invokeEvent(Event event) {
        List<Subscriber> eventSubscribers = subscribers.get(event.getClass());
        if (eventSubscribers != null) {
            synchronized (this) {
                for (Subscriber subscriber : eventSubscribers) {
                    subscriber.invoke(event);
                }
            }
        }
    }

    private record Subscriber(Object target, Method method, int priority) {
        private Subscriber(Object target, Method method, int priority) {
            this.target = target;
            this.method = method;
            this.priority = priority;
            this.method.setAccessible(true);
        }

        public void invoke(Event event) {
            try {
                method.invoke(target, event);
            } catch (Exception e) {
                throw new RuntimeException("Error invoking subscriber method", e);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Subscriber that = (Subscriber) obj;
            return target.equals(that.target) && method.equals(that.method);
        }
    }
}