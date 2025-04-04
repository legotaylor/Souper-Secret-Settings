#version 150

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Swap;
uniform vec3 Add;
uniform vec3 Sub;
uniform vec3 Mul;
uniform vec3 Div;
uniform vec3 Pow;
uniform vec3 InA;
uniform vec3 InB;
uniform vec3 Out;


void main() {
    vec3 colA = texture(InSampler, texCoord).rgb;
    vec3 colB = texture(BaseSampler, texCoord).rgb;

    vec3 s = sin(Swap*1.57079632679);
    vec3 c = cos(Swap*1.57079632679);
    vec3 a = abs(colA * c - colB * s);
    vec3 b = abs(colA * s + colB * c);

    vec3 col = (a+b)*Add + (a-b)*Sub + (a*b)*Mul + (a/b)*Div + pow(a,b)*Pow;
    col = a*InA + b*InB + clamp(col, 0, 1)*Out;

    fragColor = vec4(col, 1.0);
}
