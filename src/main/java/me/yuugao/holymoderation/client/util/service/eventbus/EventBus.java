package me.yuugao.holymoderation.client.util.service.eventbus;

import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;
import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

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
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1 && Event.class.isAssignableFrom(parameterTypes[0])) {
                    Class<?> eventType = parameterTypes[0];
                    Subscribe annotation = method.getAnnotation(Subscribe.class);
                    int priority = annotation.priority();
                    Subscriber subscriber = new Subscriber(object, method, priority, loggerService);

                    if (subscribers.values().stream().anyMatch(list ->
                            list.stream().anyMatch(e -> e.method().equals(method))))
                        return;

                    subscribers.computeIfAbsent(eventType, k ->
                            new CopyOnWriteArrayList<>()).add(subscriber);
                    subscribers.get(eventType).sort((s1, s2) ->
                            Integer.compare(s2.priority(), s1.priority()));
                    loggerService.debug("Registered new subscriber: %s.".formatted(subscriber));
                }
            }
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

    @SuppressWarnings("unchecked")
    public <T> T getModule(Class<T> moduleClass) {
        return (T) subscribers.values().stream()
                .flatMap(List::stream)
                .map(Subscriber::target)
                .filter(moduleClass::isInstance)
                .findFirst()
                .orElse(null);
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

    @DontObf(ObfRule.MAP_METHOD)
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