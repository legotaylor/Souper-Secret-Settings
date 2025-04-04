#version 150

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform vec3 Color1;
uniform vec3 Color2;
uniform vec3 Color3;
uniform vec3 Color4;
uniform vec4 Base;
uniform vec2 Offset1;
uniform vec2 Offset2;
uniform vec2 Offset3;
uniform vec2 Offset4;
uniform vec4 Threshold;
uniform vec3 Boost;

void main() {
    vec3 col = mix(texture(BaseSampler, texCoord).rgb, Base.rgb, Base.a);

    if (texture(InSampler, texCoord + Offset1*oneTexel).x > Threshold.x) {
        col *= Color1 * Boost;
    }
    if (texture(InSampler, texCoord + Offset2*oneTexel).y > Threshold.y) {
        col *= Color2 * Boost;
    }
    if (texture(InSampler, texCoord + Offset3*oneTexel).z > Threshold.z) {
        col *= Color3 * Boost;
    }
    if (texture(InSampler, texCoord + Offset4*oneTexel).w > Threshold.w) {
        col *= Color4 * Boost;
    }

    fragColor = vec4(col, 1.0);
}
