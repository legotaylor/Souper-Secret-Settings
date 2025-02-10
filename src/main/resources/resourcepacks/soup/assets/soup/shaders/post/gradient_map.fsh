#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Gray;
uniform float Colors;
uniform float Rounding;
uniform vec3 Color1;
uniform vec3 Color2;
uniform vec3 Color3;
uniform vec3 Color4;
uniform vec3 Color5;
uniform vec3 Color6;
uniform vec3 Color7;
uniform vec3 Color8;
uniform float luminance_alpha_smooth;

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
    vec3 col = texture(InSampler, texCoord).rgb;
    float t = dot(col, Gray)*Colors;
    if (t < 0) {
        t += 7;
    }
    t = mix(t, Rounding > 0 ? ceil(t) : floor(t), abs(Rounding));
    fragColor = vec4(mix(col, mix(getColor(floor(t)), getColor(ceil(t)), fract(t)), luminance_alpha_smooth), 1.0);
}
