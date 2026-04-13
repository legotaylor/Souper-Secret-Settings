#version 330

uniform sampler2D InSampler;

layout(std140) uniform NormaliseConfig {
    uniform vec3 Levels;
    uniform float Alpha;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    float m = max(max(col.r,col.g),col.b);
    vec3 normalised = m == 0 ? col : col/m;

    fragColor = vec4(mix(col, normalised*ceil(m*Levels)/Levels, Alpha), 1.0);
}
