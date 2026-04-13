#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform PrintMaskConfig {
    vec3 Color1;
    vec3 Color2;
    vec3 Color3;
    vec3 Color4;
    vec3 Base;
    int Combinations;
    float Dithering;
};

in vec2 texCoord;

out vec4 fragColor;

vec3 palette[16] = vec3[16](
    Base,
    Base * Color1,
    Base * Color2,
    Base * Color3,
    Base * Color4,
    Base * Color1 * Color2,
    Base * Color1 * Color3,
    Base * Color1 * Color4,
    Base * Color2 * Color3,
    Base * Color2 * Color4,
    Base * Color3 * Color4,
    Base * Color1 * Color2 * Color3,
    Base * Color2 * Color3 * Color4,
    Base * Color3 * Color4 * Color1,
    Base * Color4 * Color1 * Color2,
    Base * Color1 * Color2 * Color3 * Color4
);

vec4 reference[16] = vec4[16](
    vec4(0,0,0,0),
    vec4(1,0,0,0),
    vec4(0,1,0,0),
    vec4(0,0,1,0),
    vec4(0,0,0,1),
    vec4(1,1,0,0),
    vec4(1,0,1,0),
    vec4(1,0,0,1),
    vec4(0,1,1,0),
    vec4(0,1,0,1),
    vec4(0,0,1,1),
    vec4(1,1,1,0),
    vec4(0,1,1,1),
    vec4(1,0,1,1),
    vec4(1,1,0,1),
    vec4(1,1,1,1)
);

void main() {
    vec3 baseCol = texture(InSampler, texCoord).rgb;

    float minDistance = distance(palette[0], baseCol);
    int minI = 0;
    float secondDistance = minDistance;
    int secondI = 0;

    for (int i = 1; i < Combinations; i++) {
        float d = distance(palette[i], baseCol);
        if (d < minDistance) {
            secondDistance = minDistance;
            secondI = minI;
            minDistance = d;
            minI = i;
        }
        else if (d < secondDistance) {
            secondDistance = d;
            secondI = i;
        }
    }

    if (Dithering > 0.5) {
        ivec2 coord = ivec2((texCoord*InSize)/Dithering);
        if ((coord.x + coord.y)%2 == 0 && minDistance > distance((palette[minI]+palette[secondI])/2.0, baseCol)) {
            minI = secondI;
        }
    }

    fragColor = reference[minI];
}
