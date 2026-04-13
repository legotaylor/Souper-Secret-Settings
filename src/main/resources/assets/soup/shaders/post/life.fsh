#version 330

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform LifeConfig {
    vec4 OnKernelOrtho;
    vec4 OnKernelDiag;

    vec4 OffKernelOrtho;
    vec4 OffKernelDiag;

    vec4 KernelDistance;

    vec3 RedKernelMask;
    vec3 RedTurnOn;
    vec3 RedTurnOff;

    vec3 GreenKernelMask;
    vec3 GreenTurnOn;
    vec3 GreenTurnOff;

    vec3 BlueKernelMask;
    vec3 BlueTurnOn;
    vec3 BlueTurnOff;

    vec3 IsOnThreshold;
};

in vec2 texCoord;

out vec4 fragColor;

float total(vec3 v) {
    return v.r + v.g + v.b;
}

float neighbourCount(vec4 kernelOrtho, vec4 kernelDiag, vec3 mask, vec3 posZP, vec3 posPZ, vec3 posZN, vec3 posNZ, vec3 posPP, vec3 posPN, vec3 posNN, vec3 posNP) {
    return kernelOrtho.x*total(posZP*mask) + kernelOrtho.y*total(posPZ*mask) + kernelOrtho.z*total(posZN*mask) + kernelOrtho.w*total(posNZ*mask) +
           kernelDiag.x *total(posPP*mask) + kernelDiag.y *total(posPN*mask) + kernelDiag.z *total(posNN*mask) + kernelDiag.w *total(posNP*mask);
}

bool passesRange(float value, vec3 range) {
    if (range.z > 0.5) {
        return range.x < value+0.1 && value-0.1 < range.y;
    }
    return range.x > value+0.1 || value-0.1 > range.y;
}

float life(float current, vec3 mask, vec3 turnOnRange, vec3 turnOffRange, vec3 posZP, vec3 posPZ, vec3 posZN, vec3 posNZ, vec3 posPP, vec3 posPN, vec3 posNN, vec3 posNP) {
    vec4 kernelOrtho;
    vec4 kernelDiag;
    vec3 changeStateRange;

    if (current > 0.5) {
        kernelOrtho = OnKernelOrtho;
        kernelDiag = OnKernelDiag;
        changeStateRange = turnOffRange;
    } else {
        kernelOrtho = OffKernelOrtho;
        kernelDiag = OffKernelDiag;
        changeStateRange = turnOnRange;
    }

    float neighbours = neighbourCount(kernelOrtho, kernelDiag, mask, posZP, posPZ, posZN, posNZ, posPP, posPN, posNN, posNP);
    if (passesRange(neighbours, changeStateRange)) {
        return 1.0-current;
    }

    return current;
}

vec2 offset(float dist, float angle) {
    float rad = angle*6.28318530718;
    return vec2(sin(rad), cos(rad)) * dist;
}

void main() {
    vec2 oneTexel = 1.0 / InSize;

    vec3 posZP = texture(PrevSampler, texCoord+offset(KernelDistance.x, KernelDistance.y     )*oneTexel).rgb;
    vec3 posPZ = texture(PrevSampler, texCoord+offset(KernelDistance.x, KernelDistance.y+0.25)*oneTexel).rgb;
    vec3 posZN = texture(PrevSampler, texCoord+offset(KernelDistance.x, KernelDistance.y+0.5 )*oneTexel).rgb;
    vec3 posNZ = texture(PrevSampler, texCoord+offset(KernelDistance.x, KernelDistance.y-0.25)*oneTexel).rgb;

    vec3 posPP = texture(PrevSampler, texCoord+offset(KernelDistance.z, KernelDistance.w     )*oneTexel).rgb;
    vec3 posPN = texture(PrevSampler, texCoord+offset(KernelDistance.z, KernelDistance.w+0.25)*oneTexel).rgb;
    vec3 posNN = texture(PrevSampler, texCoord+offset(KernelDistance.z, KernelDistance.w+0.5 )*oneTexel).rgb;
    vec3 posNP = texture(PrevSampler, texCoord+offset(KernelDistance.z, KernelDistance.w-0.25)*oneTexel).rgb;

    vec3 current = texture(PrevSampler, texCoord).rgb;

    vec3 iteration = vec3(
        life(current.r, RedKernelMask,   RedTurnOn,   RedTurnOff,   posZP, posPZ, posZN, posNZ, posPP, posPN, posNN, posNP),
        life(current.g, GreenKernelMask, GreenTurnOn, GreenTurnOff, posZP, posPZ, posZN, posNZ, posPP, posPN, posNN, posNP),
        life(current.b, BlueKernelMask,  BlueTurnOn,  BlueTurnOff,  posZP, posPZ, posZN, posNZ, posPP, posPN, posNN, posNP)
    );

    vec3 new = step(IsOnThreshold, texture(InSampler, texCoord).rgb);
    fragColor = vec4(max(new, iteration), 1.0);
}
