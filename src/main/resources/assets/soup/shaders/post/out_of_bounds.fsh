#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D PrevOutSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Threshold;
uniform vec2 Offset;
uniform vec2 luminance_clipping;

float LinearizeDepth(float depth) {
    return (luminance_clipping.x*luminance_clipping.y) / (depth * (luminance_clipping.x - luminance_clipping.y) + luminance_clipping.y);
}

void main(){
    vec4 col = texture(InSampler, texCoord);
    float depth = texture(InDepthSampler, texCoord).r;
    if (depth == 1.0 || LinearizeDepth(depth) > Threshold) {
        col = texture(PrevOutSampler, texCoord + Offset*oneTexel);
    }
    fragColor = vec4(col.rgb, 1.0);
}
