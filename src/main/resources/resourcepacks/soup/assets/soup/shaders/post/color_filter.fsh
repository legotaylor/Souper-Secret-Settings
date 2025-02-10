#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform vec3 Gray;
uniform vec3 Mask;
uniform vec3 Threshold;
uniform float luminance_alpha_smooth;

float maxRGB(vec3 v) {
    return max(max(v.r, v.g), v.b);
}

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = maxRGB(base*Mask) > maxRGB(base*Threshold) ? base.rgb : vec3(dot(base, Gray));
    fragColor = vec4(mix(base, col, luminance_alpha_smooth), 1.0);
}
