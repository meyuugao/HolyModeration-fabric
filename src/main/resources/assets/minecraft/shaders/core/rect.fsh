#version 150

in vec4 vertexColor;

uniform vec4 Color;

out vec4 fragColor;

void main() {
    fragColor = vertexColor * Color;
}