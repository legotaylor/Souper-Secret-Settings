#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform vec4 OnKernelOrtho;
uniform vec4 OnKernelDiag;

uniform vec4 OffKernelOrtho;
uniform vec4 OffKernelDiag;

uniform vec3 RedKernelMask;
uniform vec3 RedTurnOn;
uniform vec3 RedTurnOff;

uniform vec3 GreenKernelMask;
uniform vec3 GreenTurnOn;
uniform vec3 GreenTurnOff;

uniform vec3 BlueKernelMask;
uniform vec3 BlueTurnOn;
uniform vec3 BlueTurnOff;

uniform float IsOnThreshold;

out vec4 fragColor;

float total(vec3 v) {
    return v.r + v.g + v.b;
}

float neighbourCount(vec4 kernelOrtho, vec4 kernelDiag, vec3 mask, vec3 posPP, vec3 posPZ, vec3 posPN, vec3 posNP, vec3 posNZ, vec3 posNN, vec3 posZP, vec3 posZN) {
    return kernelOrtho.x*total(posZP*mask) + kernelOrtho.y*total(posPZ*mask) + kernelOrtho.z*total(posZN*mask) + kernelOrtho.w*total(posNZ*mask) +
           kernelDiag.x *total(posPP*mask) + kernelDiag.y *total(posPN*mask) + kernelDiag.z *total(posNN*mask) + kernelDiag.w *total(posNP*mask);
}

bool passesRange(float value, vec3 range) {
    if (range.z > 0.5) {
        return range.x < value+0.1 && value-0.1 < range.y;
    }
    return range.x > value+0.1 || value-0.1 > range.y;
}

float life(float current, vec3 mask, vec3 turnOnRange, vec3 turnOffRange, vec3 posPP, vec3 posPZ, vec3 posPN, vec3 posNP, vec3 posNZ, vec3 posNN, vec3 posZP, vec3 posZN) {
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

    float neighbours = neighbourCount(kernelOrtho, kernelDiag, mask, posPP, posPZ, posPN, posNP, posNZ, posNN, posZP, posZN);
    if (passesRange(neighbours, changeStateRange)) {
        return 1.0-current;
    }

    return current;
}

void main() {
    vec3 posPP = texture(PrevSampler, texCoord+vec2( oneTexel.x,  oneTexel.y)).rgb;
    vec3 posPZ = texture(PrevSampler, texCoord+vec2( oneTexel.x,  0         )).rgb;
    vec3 posPN = texture(PrevSampler, texCoord+vec2( oneTexel.x, -oneTexel.y)).rgb;
    vec3 posNP = texture(PrevSampler, texCoord+vec2(-oneTexel.x,  oneTexel.y)).rgb;
    vec3 posNZ = texture(PrevSampler, texCoord+vec2(-oneTexel.x,  0         )).rgb;
    vec3 posNN = texture(PrevSampler, texCoord+vec2(-oneTexel.x, -oneTexel.y)).rgb;
    vec3 posZP = texture(PrevSampler, texCoord+vec2( 0         ,  oneTexel.y)).rgb;
    vec3 posZN = texture(PrevSampler, texCoord+vec2( 0         , -oneTexel.y)).rgb;

    vec3 current = texture(PrevSampler, texCoord).rgb;

    vec3 iteration = vec3(
        life(current.r, RedKernelMask,   RedTurnOn,   RedTurnOff,   posPP, posPZ, posPN, posNP, posNZ, posNN, posZP, posZN),
        life(current.g, GreenKernelMask, GreenTurnOn, GreenTurnOff, posPP, posPZ, posPN, posNP, posNZ, posNN, posZP, posZN),
        life(current.b, BlueKernelMask,  BlueTurnOn,  BlueTurnOff,  posPP, posPZ, posPN, posNP, posNZ, posNN, posZP, posZN)
    );

    vec3 new = step(IsOnThreshold, texture(InSampler, texCoord).rgb);
    fragColor = vec4(max(new, iteration), 1.0);
}
