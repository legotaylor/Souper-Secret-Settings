#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform DepthLineConfig {
    float Pow;
    vec2 StartOffset;
    mat2 StartMatrix;
    vec2 EndOffset;
    mat2 EndMatrix;
    float Wrapping;
    vec3 Mix;
};

in vec2 texCoord;

out vec4 fragColor;

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

void main(){
    float depth = pow(texture(InDepthSampler, texCoord).r, Pow);
    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, wrapTexture(InSampler, mix((texCoord*StartMatrix) + StartOffset, (texCoord*EndMatrix) + EndOffset, depth)).rgb, Mix), 1.0);
}
