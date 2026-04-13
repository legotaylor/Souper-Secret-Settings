#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
    float Curve;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec2 oneTexel = 1.0 / InSize;

    vec3 col = vec3(0);
    float total = 0;
    for (float i = 0; i <= Radius; i++) {
        float weight = 1.0-(i/Radius);
        weight = mix(weight, weight * weight, Curve);
        total += weight;
        col += texture(InSampler, texCoord-(BlurDir*oneTexel*i)).rgb * weight;
    }
    col /= total;

    fragColor = vec4(col, 1.0);
}
