#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Center;
uniform float Multiplier;
uniform float Steps;
uniform float luminance_alpha_smooth;

vec4 combine(vec4 a, vec4 b, float fade) {
    return mix(vec4(max(a.r,b.r), max(a.g,b.g), max(a.b,b.b), 1.0), a, fade);
}

void main(){
    vec2 centeredCoord = texCoord - Center;
    vec4 base = texture(InSampler, texCoord);
    vec4 col = base;
    float runningScale = 1.0;
    for(float x = 0.0; x <= Steps; x += 1.0) {
        runningScale *= Multiplier;
        col = combine(col, texture(InSampler, (centeredCoord*runningScale)+Center), x/Steps);
    }

    fragColor = vec4(mix(base, col, luminance_alpha_smooth).rgb, 1.0);
}
