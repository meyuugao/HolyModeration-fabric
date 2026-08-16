#version 330

layout(std140) uniform Radius {
    float radius;
};

layout(std140) uniform Size {
    vec2 size;
};

layout(std140) uniform Color {
    vec4 color;
};

layout(std140) uniform BlurWidth {
    float blurWidth;
};

in vec2 uv;

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = uv - size * 0.5;
    float d = sdRoundRect(p, size * 0.5, radius);

    if (d > blurWidth) {
        discard;
    } else if (d > 0.0) {
        float alpha = color.a * (1.0 - d / blurWidth);
        fragColor = vec4(color.rgb, alpha);
    } else {
        fragColor = color;
    }
}
