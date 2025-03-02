#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Offset;
uniform float Centering;
uniform float luminance_alpha_smooth;

void main() {
    float amount = ((int(texCoord.x/oneTexel.x)+int(texCoord.y/oneTexel.y))%2)+Centering;
    vec4 col = texture(InSampler, mod(texCoord + Offset*amount, vec2(1,1)));
    fragColor = vec4(mix(texture(InSampler, texCoord), col, luminance_alpha_smooth).rgb, 1.0);
}
