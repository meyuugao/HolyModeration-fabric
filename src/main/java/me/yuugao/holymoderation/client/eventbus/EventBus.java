package me.yuugao.holymoderation.client.eventbus;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventBus {
    private final Map<Class<?>, Set<Subscriber>> subscribers = new ConcurrentHashMap<>();

    public void register(Object object) {
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && Event.class.isAssignableFrom(parameterTypes[0])) {
                    Class<?> eventType = parameterTypes[0];
                    Subscriber subscriber = new Subscriber(object, method);
                    subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(subscriber);
                }
            }
        }
    }

    public void unregister(Object object) {
        for (Set<Subscriber> subscriberSet : subscribers.values()) {
            subscriberSet.removeIf(subscriber -> subscriber.target().equals(object));
        }
    }

    public void invokeEvent(Event event) {
        Set<Subscriber> eventSubscribers = subscribers.get(event.getClass());
        if (eventSubscribers != null) {
            for (Subscriber subscriber : eventSubscribers) {
                subscriber.invoke(event);
            }
        }
    }

    public void clear() {
        subscribers.clear();
    }

    private record Subscriber(Object target, Method method) {
        private Subscriber(Object target, Method method) {
            this.target = target;
            this.method = method;
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