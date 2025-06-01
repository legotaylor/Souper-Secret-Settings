#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D PrevOutSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Threshold;
uniform vec3 Color;
uniform vec2 Pixels;
uniform float Requirements;
uniform vec2 Offset;
uniform vec3 Amount;
uniform vec2 luminance_clipping;

float LinearizeDepth(float depth) {
    return (luminance_clipping.x*luminance_clipping.y) / (depth * (luminance_clipping.x - luminance_clipping.y) + luminance_clipping.y);
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
