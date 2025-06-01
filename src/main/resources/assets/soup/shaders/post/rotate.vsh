#version 150

in vec4 Position;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform vec2 ScreenSize;

out vec2 texCoord;

uniform float Angle;
uniform vec2 X;
uniform vec2 Y;
uniform vec2 Offset;
uniform float Squish;

vec2 scale = vec2(mix(1.0, OutSize.x/OutSize.y, Squish), 1.0);

vec2 rotate(vec2 v, float angle) {
    v *= scale;
    float radians = angle / 57.2957795131;
    float s = sin(radians);
    float c = cos(radians);
    return vec2(v.x*c - v.y*s, v.x*s + v.y*c);
}

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy / OutSize;

    vec2 centered = texCoord-vec2(0.5);
    vec2 v = mat3x2(X, Y, Offset) * vec3(rotate(centered, Angle), 1.0);
    v /= scale;
    texCoord = v + vec2(0.5);
}
