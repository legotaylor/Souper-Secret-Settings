#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int PixelSize;
uniform float luminance_alpha_smooth;

void main(){
    vec2 roundedPosition = floor(texCoord/oneTexel / PixelSize) * PixelSize;

    roundedPosition = roundedPosition * oneTexel;
    vec4 ditherTarget = texture(InSampler, roundedPosition);

    fragColor = vec4(mix(texture(InSampler, texCoord), ditherTarget, luminance_alpha_smooth).rgb, 1.0);
}
