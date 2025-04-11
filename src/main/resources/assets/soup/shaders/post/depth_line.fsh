#version 150

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Pow;
uniform vec2 StartOffset;
uniform mat2 StartMatrix;
uniform vec2 EndOffset;
uniform mat2 EndMatrix;
uniform float Wrapping;
uniform vec3 Mix;

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

void main(){
    float depth = pow(texture(DepthSampler, texCoord).r, Pow);
    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, wrapTexture(InSampler, mix((texCoord*StartMatrix) + StartOffset, (texCoord*EndMatrix) + EndOffset, depth)).rgb, Mix), 1.0);
}
