#version 330

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

layout(std140) uniform BlendConfig {
    vec3 Swap;
    vec3 Add;
    vec3 Sub;
    vec3 Mul;
    vec3 Div;
    vec3 Pow;
    vec3 Xor;
    vec3 InA;
    vec3 InB;
    vec3 Out;
};

in vec2 texCoord;

out vec4 fragColor;

vec3 xor(vec3 a, vec3 b) {
    return vec3(ivec3(a*255.0) ^ ivec3(b*255.0))/255.0;
}

void main() {
    vec3 colA = texture(InSampler, texCoord).rgb;
    vec3 colB = texture(BaseSampler, texCoord).rgb;

    vec3 s = sin(Swap*1.57079632679);
    vec3 c = cos(Swap*1.57079632679);
    vec3 a = abs(colA * c - colB * s);
    vec3 b = abs(colA * s + colB * c);

    vec3 col = (a+b)*Add + (a-b)*Sub + (a*b)*Mul + (a/b)*Div + pow(a,b)*Pow + xor(a,b)*Xor;
    col = a*InA + b*InB + clamp(col, 0, 1)*Out;

    fragColor = vec4(col, 1.0);
}
