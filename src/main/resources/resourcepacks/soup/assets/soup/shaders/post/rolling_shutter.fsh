#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform float luminance_time;
uniform float Threshold;
uniform vec2 Direction;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    vec3 prev = texture(PrevSampler, texCoord).rgb;

    float d = texCoord.y*Direction.y + texCoord.x*Direction.x;

    fragColor = vec4(mix(prev, col, fract(luminance_time+d) < Threshold ? 1.0 : 0.0), 1.0);
}
