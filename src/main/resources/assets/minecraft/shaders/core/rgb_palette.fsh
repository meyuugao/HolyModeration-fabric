#version 330

layout(std140) uniform Radius {
    float Radius;
};

layout(std140) uniform OutlineColor {
    vec4 OutlineColor;
};

layout(std140) uniform OutlineWidth {
    float OutlineWidth;
};

layout(std140) uniform Size {
    vec2 Size;
};

in vec2 uv;

out vec4 fragColor;

const float PI = 3.14159265359;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 center = Size * 0.5;
    vec2 pos = uv - center;
    float dist = length(pos);

    if (dist > Radius + OutlineWidth) {
        discard;
    }

    if (dist > Radius) {
        float t = (dist - Radius) / OutlineWidth;
        float alpha = 1.0 - smoothstep(0.0, 1.0, t);
        fragColor = vec4(OutlineColor.rgb, OutlineColor.a * alpha);
        return;
    }

    float angle = atan(pos.y, pos.x);
    float hue = (angle + PI) / (2.0 * PI);
    float saturation = dist / Radius;
    float value = 1.0;

    vec3 color = hsv2rgb(vec3(hue, saturation, value));
    fragColor = vec4(color, 1.0);
}
