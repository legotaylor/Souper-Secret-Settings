#version 150

uniform sampler2D InSampler;
uniform sampler2D LookupSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float GridSize;
uniform float Iterations;
uniform float luminance_alpha_smooth;

out vec4 fragColor;

const float scale = 255.0 / 256.0;

vec3 lookup(vec3 col) {
    col *= scale;
    vec2 coord = vec2((col.r + floor(fract(col.b * GridSize))) / GridSize, (col.g + floor(col.b * GridSize)) / GridSize);
    return texture(LookupSampler, coord).rgb;
}

void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = base;

    float a = abs(Iterations);
    float s = sign(Iterations);
    for (int i = 0; i < a; i++) {
        vec3 target = lookup(col);
        col = mix(col, target, s*min(a-i,1));
    }

    fragColor = vec4(mix(base, col, luminance_alpha_smooth), 1.0);
}
