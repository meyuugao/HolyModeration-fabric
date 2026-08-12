package me.yuugao.holymoderation.client.util.service.eventbus;

import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Setter;

public class EventBus {
    private final Map<Class<?>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();
    @Setter
    private LoggerService loggerService;

    public void register(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Subscribe.class)) continue;
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) continue;

            Class<?> eventType = parameterTypes[0];
            List<Subscriber> list = subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());

            // Dedup: a @Subscribe method maps to exactly one event type, so it is enough to check
            // that single list (the old code scanned every event type's list). Continue (not return)
            // so the remaining methods of the same object still register.
            boolean alreadyRegistered = false;
            for (Subscriber existing : list) {
                if (existing.method().equals(method)) {
                    alreadyRegistered = true;
                    break;
                }
            }
            if (alreadyRegistered) continue;

            int priority = method.getAnnotation(Subscribe.class).priority();
            Subscriber subscriber = new Subscriber(object, method, priority, loggerService);
            list.add(subscriber);
            list.sort((s1, s2) -> Integer.compare(s2.priority(), s1.priority()));
            loggerService.debug("Registered new subscriber: %s.".formatted(subscriber));
        }
    }

    public void unregister(Object object) {
        for (List<Subscriber> subscriberList : subscribers.values()) {
            subscriberList.removeIf(subscriber -> subscriber.target.equals(object));
        }
        loggerService.debug("Unregistered module: %s.".formatted(object));
    }

    public void clear() {
        subscribers.clear();
        loggerService.debug("All subscribers cleared.");
    }

    public void invokeEvent(Event event) {
        List<Subscriber> eventSubscribers = subscribers.get(event.getClass());
        if (eventSubscribers != null) {
            executeSubscribersSync(eventSubscribers, event);
        }
    }

    private void executeSubscribersSync(List<Subscriber> subscribers, Event event) {
        for (Subscriber subscriber : subscribers) {
            if (event.isCancelled() || !this.subscribers.get(event.getClass()).contains(subscriber)) {
                break;
            }

            subscriber.invoke(event);
        }
    }

    private record Subscriber(Object target, Method method, int priority, LoggerService loggerService) {
        private Subscriber(Object target, Method method, int priority, LoggerService loggerService) {
            this.target = target;
            this.method = method;
            this.priority = priority;
            this.loggerService = loggerService;
            this.method.setAccessible(true);
        }

        public void invoke(Event event) {
            try {
                method.invoke(target, event);
            } catch (InvocationTargetException e) {
                loggerService.exception("Исключение в Subscriber/invoke: %s в классе %s: %s".formatted(method.getName(), method.getDeclaringClass(), e.getCause()));
            } catch (ReflectiveOperationException e) {
                loggerService.exception("Исключение в Subscriber/invoke: %s в классе %s: %s".formatted(method.getName(), method.getDeclaringClass(), e));
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