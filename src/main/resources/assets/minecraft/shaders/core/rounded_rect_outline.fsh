#version 330

layout(std140) uniform Radius {
    float Radius;
};

layout(std140) uniform Size {
    vec2 Size;
};

layout(std140) uniform Color {
    vec4 Color;
};

layout(std140) uniform OutlineColor {
    vec4 OutlineColor;
};

layout(std140) uniform OutlineWidth {
    float OutlineWidth;
};

in vec2 uv;

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = uv - Size * 0.5;
    float d = sdRoundRect(p, Size * 0.5, Radius);

    if (d > 0.0) {
        discard;
    } else if (d > -OutlineWidth) {
        float t = smoothstep(-OutlineWidth, 0.0, d);
        fragColor = mix(Color, OutlineColor, t);
    } else {
        fragColor = Color;
    }
}
