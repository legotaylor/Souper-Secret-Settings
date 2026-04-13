#version 330

uniform sampler2D InSampler;
uniform sampler2D SubtractSampler;

layout(std140) uniform DifferenceConfig {
    vec3 Color;
    float Scale;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 col = Color + (texture(InSampler, texCoord).rgb - texture(SubtractSampler, texCoord).rgb) * Scale;
    fragColor = vec4(col, 1.0);
}
