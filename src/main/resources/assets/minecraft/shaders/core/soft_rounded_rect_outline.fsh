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

layout(std140) uniform OutlineColor {
    vec4 outlineColor;
};

layout(std140) uniform OutlineWidth {
    float outlineWidth;
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
    vec2 p = uv - (size * 0.5 + vec2(blurWidth));
    float d = sdRoundRect(p, size * 0.5, radius);

    if (d > blurWidth) {
        discard;
    } else if (d > 0.0) {
        float alpha = outlineColor.a * (1.0 - d / blurWidth);
        fragColor = vec4(outlineColor.rgb, alpha);
    } else if (d > -outlineWidth) {
        float t = smoothstep(-outlineWidth, 0.0, d);
        fragColor = mix(color, outlineColor, t);
    } else {
        fragColor = color;
    }
}
