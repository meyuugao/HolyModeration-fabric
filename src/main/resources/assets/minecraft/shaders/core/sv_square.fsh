#version 330

layout(std140) uniform Radius {
    float radius;
};

layout(std140) uniform Size {
    vec2 size;
};

layout(std140) uniform Hue {
    float hue;
};

layout(std140) uniform OutlineColor {
    vec4 outlineColor;
};

layout(std140) uniform OutlineWidth {
    float outlineWidth;
};

in vec2 uv;

out vec4 fragColor;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = uv - size * 0.5;
    float d = sdRoundRect(p, size * 0.5, radius);
    if (d > outlineWidth) {
        discard;
    }

    float sat = clamp(uv.x / max(size.x, 0.0001), 0.0, 1.0);
    float val = clamp(1.0 - uv.y / max(size.y, 0.0001), 0.0, 1.0);
    vec3 col = hsv2rgb(vec3(fract(hue), sat, val));

    if (d > 0.0) {
        float t = clamp(d / max(outlineWidth, 0.0001), 0.0, 1.0);
        float a = 1.0 - smoothstep(0.0, 1.0, t);
        fragColor = vec4(outlineColor.rgb, outlineColor.a * a);
        return;
    }

    fragColor = vec4(col, 1.0);
}
