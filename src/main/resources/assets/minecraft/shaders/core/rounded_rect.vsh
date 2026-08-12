#version 150

in vec4 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 uv;

void main() {
    gl_Position = ProjMat * ModelViewMat * Position;
    uv = UV0;
}