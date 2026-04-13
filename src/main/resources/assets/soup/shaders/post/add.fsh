#version 330

uniform sampler2D InSampler;
uniform sampler2D AddSampler;

layout(std140) uniform AddConfig {
    float Mix;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 CurrTexel = texture(InSampler, texCoord).rgb;
    vec3 AddTexel = texture(AddSampler, texCoord).rgb;

    fragColor = vec4(mix(CurrTexel, AddTexel + CurrTexel, Mix), 1.0);
}
