#version 150

in vec2 texCoord;
out vec4 FragColor;

uniform vec2 Size;
uniform float Radius;
uniform vec4 Color;

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + vec2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = texCoord * Size - 0.5 * Size; // Центрируем координаты
    float d = sdRoundedBox(p, 0.5 * Size, Radius);
    float w = fwidth(d) * 2.0; // Увеличиваем сглаживание для большей чёткости
    float a = 1.0 - smoothstep(-w, w, d);
    FragColor = vec4(Color.rgb, Color.a * a);
}