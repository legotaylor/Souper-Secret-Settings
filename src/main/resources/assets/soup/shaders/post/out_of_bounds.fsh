#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D PrevOutSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
    vec2 PrevOutSize;
};

layout(std140) uniform OutOfBoundsConfig {
    float Threshold;
    vec3 Color;
    vec2 Pixels;
    float Requirements;
    vec2 Offset;
    vec3 Amount;
    vec2 Clipping;
};

in vec2 texCoord;

out vec4 fragColor;

float LinearizeDepth(float depth) {
    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

bool threshold01(float value, float threshold) {
    if (threshold >= 0) {
        return value < threshold;
    } else {
        return value > threshold+1;
    }
}

bool threshold(float value, float threshold) {
    return threshold != 0 && (value/threshold > sign(threshold));
}

vec2 oneTexel = 1.0 / InSize;

bool ignore(vec3 col) {
    if (Requirements == 0) {
        return true;
    }

    float total = 0;
    float depth = texture(InDepthSampler, texCoord).r;
    if ((Threshold > 0 && depth == 1.0) || threshold(LinearizeDepth(depth), Threshold)) total++;
    if (threshold01(texCoord.x, Pixels.x*oneTexel.x)) total++;
    if (threshold01(texCoord.y, Pixels.y*oneTexel.y)) total++;
    if (threshold01(col.x, Color.x)) total++;
    if (threshold01(col.y, Color.y)) total++;
    if (threshold01(col.z, Color.z)) total++;
    return threshold(total, Requirements > 0 ? Requirements-0.01 : Requirements);
}

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    if (ignore(col)) {
        col = mix(col, texture(PrevOutSampler, texCoord + Offset*oneTexel).rgb, Amount.xyz);
    }

    fragColor = vec4(col, 1.0);
}
