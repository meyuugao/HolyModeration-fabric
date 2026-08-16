#version 330

layout(std140) uniform Color {
    vec4 Color;
};

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    fragColor = vertexColor * Color;
}
