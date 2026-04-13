#version 330

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

layout(std140) uniform CycleConfig {
    int Phase;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 color = texture(Phase%3 == 0 ? InSampler : PrevSampler, texCoord);
    fragColor = vec4(color.rgb, 1);
}