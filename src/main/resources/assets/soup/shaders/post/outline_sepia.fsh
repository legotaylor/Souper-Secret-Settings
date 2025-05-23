#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float LumaRamp;
uniform float LumaLevel;
uniform vec3 ColorAdd;
uniform vec3 ColorMul;
uniform vec3 Gray;
uniform vec3 Mix;
uniform float Outline;

out vec4 fragColor;

void main(){
    vec4 center = texture(InSampler, texCoord);
    vec4 up     = texture(InSampler, texCoord + vec2(        0.0, -oneTexel.y));
    vec4 up2    = texture(InSampler, texCoord + vec2(        0.0, -oneTexel.y) * 2.0);
    vec4 down   = texture(InSampler, texCoord + vec2( oneTexel.x,         0.0));
    vec4 down2  = texture(InSampler, texCoord + vec2( oneTexel.x,         0.0) * 2.0);
    vec4 left   = texture(InSampler, texCoord + vec2(-oneTexel.x,         0.0));
    vec4 left2  = texture(InSampler, texCoord + vec2(-oneTexel.x,         0.0) * 2.0);
    vec4 right  = texture(InSampler, texCoord + vec2(        0.0,  oneTexel.y));
    vec4 right2 = texture(InSampler, texCoord + vec2(        0.0,  oneTexel.y) * 2.0);
    vec4 uDiff = abs(center - up);
    vec4 dDiff = abs(center - down);
    vec4 lDiff = abs(center - left);
    vec4 rDiff = abs(center - right);
    vec4 u2Diff = abs(center - up2);
    vec4 d2Diff = abs(center - down2);
    vec4 l2Diff = abs(center - left2);
    vec4 r2Diff = abs(center - right2);
    vec4 sum = uDiff + dDiff + lDiff + rDiff + u2Diff + d2Diff + l2Diff + r2Diff;
    vec4 gray = vec4(Gray, 0.0);
    float sumLuma = 1.0 - dot(clamp(sum, 0.0, 1.0), gray)*Outline;

    // Get luminance of center pixel and adjust
    float centerLuma = dot(center + (center - pow(center, vec4(LumaRamp))), gray);

    // Quantize the luma value
    centerLuma = centerLuma - fract(centerLuma * LumaLevel) / LumaLevel;

    // Re-scale to full range
    centerLuma = centerLuma * (LumaLevel / (LumaLevel - 1.0));

    // Blend with outline
    centerLuma = centerLuma * sumLuma;

    vec3 col = vec3(centerLuma);
    col += max(ColorAdd-col, 0);
    col *= ColorMul;

    fragColor = vec4(mix(center.rgb * sumLuma, col, Mix), 1.0);
}
