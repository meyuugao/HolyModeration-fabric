#version 150

in vec2 uv;

uniform float radius;
uniform vec2 size;
uniform vec4 color;

out vec4 fragColor;

float sdRoundRect(vec2 p, vec2 b, float r){
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = uv - size * 0.5;
    float d = sdRoundRect(p, size * 0.5, radius);
    if (d > 0.0) discard;
    fragColor = color;
}