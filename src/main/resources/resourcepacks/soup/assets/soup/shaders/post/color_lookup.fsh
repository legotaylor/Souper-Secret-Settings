#version 150

uniform sampler2D InSampler;
uniform sampler2D LookupSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float GridSize;
uniform int Iterations;
uniform float luminance_alpha_smooth;

out vec4 fragColor;

const float scale = 255.0 / 256.0;

void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = base;

    for (int i = 0; i < Iterations; i++) {
        col *= scale;
        vec2 coord = vec2((col.r + floor(fract(col.b * GridSize))) / GridSize, (col.g + floor(col.b * GridSize)) / GridSize);
        col = texture(LookupSampler, coord).rgb;
    }

    fragColor = vec4(mix(base, col, luminance_alpha_smooth), 1.0);
}
