#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Gray;
uniform float Colors;

uniform vec3 Color1;
uniform vec3 Color2;
uniform vec3 Color3;
uniform vec3 Color4;
uniform vec3 Color5;
uniform vec3 Color6;
uniform vec3 Color7;
uniform vec3 Color8;

vec3 colors[8] = vec3[](
    Color1,
    Color2,
    Color3,
    Color4,
    Color5,
    Color6,
    Color7,
    Color8
);

vec3 getColor(float index) {
    return colors[int(mod(index, 8))];
}

void main(){
    float t = dot(texture(InSampler, texCoord).rgb, Gray)*Colors;
    if (t < 0) {
        t += 7;
    }
    fragColor = vec4(mix(getColor(floor(t)), getColor(ceil(t)), fract(t)), 1.0);
}
