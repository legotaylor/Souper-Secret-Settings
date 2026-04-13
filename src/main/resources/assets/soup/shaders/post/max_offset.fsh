#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};


layout(std140) uniform MaxOffsetConfig {
    vec2 Direction;
    float Steps;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 maxCol = vec3(0);
    vec3 maxOffset = vec3(0);

    for (float i = 0; i < Steps; i++) {
        float d = i-Steps/2.0;
        vec2 offset = vec2(d) * Direction;
        vec3 new = texture(InSampler, texCoord + offset / InSize).rgb;

        vec3 mask = step(maxCol, new);
        maxOffset *= vec3(1)-mask;
        maxOffset += mask*d;
        maxCol = max(maxCol, new);
    }

    fragColor = vec4((maxOffset+127)/255.0, 1.0);
}
