#version 330

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

layout(std140) uniform CycleConfig {
    int Phase;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 targetColor = texture(InSampler, texCoord);
    vec4 color = texture(PrevSampler, texCoord);
    int channel = Phase%3;
    if (channel == 0) {
        color.r = targetColor.r;
    } else if (channel == 1) {
        color.g = targetColor.g;
    } else {
        color.b = targetColor.b;
    }
    fragColor = vec4(color.rgb, 1);
}