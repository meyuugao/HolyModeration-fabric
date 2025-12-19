#version 150

in vec2 uv;

uniform float Radius;
uniform vec2 Size;
uniform vec4 Color;

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = uv - Size * 0.5;
    float d = sdRoundRect(p, Size * 0.5, Radius);
    if (d > 0.0) discard;
    fragColor = Color;
}