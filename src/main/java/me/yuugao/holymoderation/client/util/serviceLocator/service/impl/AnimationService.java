package me.yuugao.holymoderation.client.util.serviceLocator.service.impl;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import lombok.Getter;

public class AnimationService extends Service {
    public float animate(float current, float target, float speed) {
        MinecraftService mc = ServiceLocator.getMinecraftService();
        float delta = mc.getClient().getLastFrameDuration();
        if (delta <= 0f) return current;

        float step = (target - current) * delta * speed;
        if (Math.abs(step) < 0.0001f) return target;

        float result = current + step;
        if (step > 0) return Math.min(result, target);
        return Math.max(result, target);
    }

    public Value createValue(float initial) {
        return new Value(initial);
    }

    public class Value {
        private float value;
        @Getter
        private float target;
        private float speed = 1f;

        public Value(float initial) {
            this.value = initial;
            this.target = initial;
        }

        public Value setTarget(float target) {
            this.target = target;
            return this;
        }

        public Value setSpeed(float speed) {
            this.speed = speed;
            return this;
        }

        public float get() {
            return value;
        }

        public void update() {
            value = animate(value, target, speed);
        }

        public boolean isFinished() {
            return Math.abs(value - target) < 0.001f;
        }

        public void reset(float val) {
            this.value = val;
            this.target = val;
        }
    }
}