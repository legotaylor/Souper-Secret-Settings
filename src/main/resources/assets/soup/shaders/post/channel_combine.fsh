#version 330

uniform sampler2D InSampler;
uniform sampler2D SecondSampler;

layout(std140) uniform ChannelCombineConfig {
    vec3 Color1;
    vec3 Color2;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec3 c1 = texture(InSampler, texCoord).rgb*Color1;
    vec3 c2 = texture(SecondSampler, texCoord).rgb*Color2;
    fragColor = vec4(c1+c2, 1.0);
}

