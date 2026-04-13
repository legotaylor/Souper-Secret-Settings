#version 330

uniform sampler2D InSampler;

layout(std140) uniform CycleConfig {
    int Phase;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 color = texture(InSampler, texCoord);
    int channel = Phase%3;
    if (channel == 0) {
        color.g = 0;
        color.b = 0;
    } else if (channel == 1) {
        color.r = 0;
        color.b = 0;
    } else {
        color.r = 0;
        color.g = 0;
    }
    fragColor = vec4(color.rgb, 1);
}