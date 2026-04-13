#version 330

uniform sampler2D InSampler;
uniform sampler2D DirectionSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform MaxOffsetConfig {
    uniform vec2 Direction;
    uniform float Steps;
    uniform vec2 Radius;
    uniform vec2 PrevDirection;
    uniform vec4 Mix;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec3 maxCol = vec3(0.0);

    vec2 radius = 1.0/Radius;

    for (float i = 0; i < Steps; i++) {
        vec2 offset = vec2(i-Steps/2.0) * Direction;
        vec3 direction = (texture(DirectionSampler, texCoord + offset * oneTexel).rgb) * 255.0 - 127;

        vec2 offsetR = offset + PrevDirection * direction.r;
        vec2 offsetG = offset + PrevDirection * direction.g;
        vec2 offsetB = offset + PrevDirection * direction.b;

        vec2 squishR = offsetR*radius;
        vec2 squishG = offsetG*radius;
        vec2 squishB = offsetB*radius;

        maxCol = max(maxCol, vec3(
            dot(squishR, squishR) > 1 ? 0 : texture(InSampler, texCoord + offsetR * oneTexel).r,
            dot(squishG, squishG) > 1 ? 0 : texture(InSampler, texCoord + offsetG * oneTexel).g,
            dot(squishB, squishB) > 1 ? 0 : texture(InSampler, texCoord + offsetB * oneTexel).b
        ));
    }

    fragColor = vec4(mix(maxCol, ((texture(DirectionSampler, texCoord + Direction * oneTexel).rgb) * 255.0 - 127)*Mix.a, Mix.rgb), 1.0);
}
