#version 150

uniform sampler2D InSampler;
uniform sampler2D LookupSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float GridSize;
uniform float luminance_alpha_smooth;

out vec4 fragColor;

void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = base*255.0/256.0;

    vec2 coord = vec2((col.r+floor(fract(col.b*GridSize)))/GridSize, (col.g+floor(col.b*GridSize))/GridSize);

    fragColor = vec4(mix(base, texture(LookupSampler, coord).rgb, luminance_alpha_smooth), 1.0);
}
