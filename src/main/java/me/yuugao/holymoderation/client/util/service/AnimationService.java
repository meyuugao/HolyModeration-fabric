package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class AnimationService {
    private final MinecraftService minecraftService;

    public float animate(float current, float target, float speed) {
        float delta = minecraftService.getClient().getLastFrameDuration();
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