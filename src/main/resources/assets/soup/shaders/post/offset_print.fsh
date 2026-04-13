#version 330

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 BaseSize;
};

layout(std140) uniform OffsetPrintConfig {
    vec3 Color1;
    vec3 Color2;
    vec3 Color3;
    vec3 Color4;
    vec4 Base;
    vec2 Offset1;
    vec2 Offset2;
    vec2 Offset3;
    vec2 Offset4;
    vec4 Threshold;
    vec3 Boost;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

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
