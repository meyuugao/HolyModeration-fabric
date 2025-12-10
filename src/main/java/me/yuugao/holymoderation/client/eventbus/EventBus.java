package me.yuugao.holymoderation.client.eventbus;

import me.yuugao.holymoderation.client.eventbus.event.Event;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.obfuscation.DontObf;
import me.yuugao.holymoderation.client.util.serviceLocator.service.LoggerService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final Map<Class<?>, List<Subscriber>> subscribers = new ConcurrentHashMap<>();
    private LoggerService loggerService;

    public void register(Object object) {
        if (object instanceof Module module) {
            Method[] methods = module.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Subscribe.class)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && Event.class.isAssignableFrom(parameterTypes[0])) {
                        Class<?> eventType = parameterTypes[0];
                        Subscribe annotation = method.getAnnotation(Subscribe.class);
                        int priority = annotation.priority();
                        Subscriber subscriber = new Subscriber(module, method, priority);

                        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(subscriber);
                        subscribers.get(eventType).sort((s1, s2) -> Integer.compare(s2.priority(), s1.priority()));
                        subscriber.target.setLogger(loggerService);
                        loggerService.getLogger().debug("Eventbus: Registered new subscriber - {}", subscriber);
                    }
                }
            }
        }
    }

    public void unregister(Object object) {
        if (object instanceof Module module) {
            for (List<Subscriber> subscriberList : subscribers.values()) {
                subscriberList.removeIf(subscriber -> subscriber.target.equals(module));
            }
            loggerService.getLogger().debug("Eventbus: Unregistered module - {}", module);
        }
    }

    public void clear() {
        subscribers.clear();
        loggerService.getLogger().debug("Eventbus: All subscribers cleared");
    }

    public void invokeEvent(Event event) {
        List<Subscriber> eventSubscribers = subscribers.get(event.getClass());
        if (eventSubscribers != null) {
            for (Subscriber subscriber : eventSubscribers) {
                if (subscribers.getOrDefault(event.getClass(), List.of()).contains(subscriber)) {
                    subscriber.invoke(event);
                    loggerService.getLogger().debug("Eventbus: Invoked event - {} for subscriber - {}", event, subscriber);
                }
            }
        }
    }

    public void setLogger(LoggerService loggerService) {
        this.loggerService = loggerService;
        getType();
    }

    public void getType() {
        System.out.println("Eventbus: Get Type");
    }

    @DontObf
    private record Subscriber(Module target, Method method, int priority) {
        private Subscriber(Module target, Method method, int priority) {
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