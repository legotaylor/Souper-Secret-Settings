#version 330

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

layout(std140) uniform Config {
    uniform float Time;
    uniform float Threshold;
    uniform vec2 Direction;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    vec3 prev = texture(PrevSampler, texCoord).rgb;

    float d = texCoord.y*Direction.y + texCoord.x*Direction.x;

    fragColor = vec4(mix(prev, col, fract(Time+d) < Threshold ? 1.0 : 0.0), 1.0);
}
