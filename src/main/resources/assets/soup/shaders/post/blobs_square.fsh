#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlobsConfig {
    vec2 Direction;
    float Steps;
    float Mode;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;
    vec3 colMax = texture(InSampler, texCoord).rgb;
    vec3 colMin = colMax;

    for (float i = 0; i < Steps; i++) {
        float offset = i-Steps/2.0;
        vec3 new = texture(InSampler, texCoord + (offset * Direction * oneTexel)).rgb;
        colMax = max(colMax, new);
        colMin = min(colMin, new);
    }

    fragColor = vec4(mix(colMax, colMin, Mode), 1.0);
}
