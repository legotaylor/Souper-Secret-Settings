#version 150

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Amount;
uniform float UseNoise;
uniform vec2 Scale;
uniform vec2 Offset;
uniform float Seed;

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(vec3 x){
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = f*f*(3.0-2.0*f);
    return mix(mix(hash(i+vec3(0, 0, x.z)),
                   hash(i+vec3(1, 0, x.z)),f.x),
               mix(hash(i+vec3(0, 1, x.z)),
                   hash(i+vec3(1, 1, x.z)),f.x),f.y);
}

void main() {
    vec4 col = texture(InSampler, texCoord);
    vec4 baseCol = texture(BaseSampler, texCoord);

    vec3 mixAmounts = Amount;
    if (UseNoise != 0) {
        mixAmounts = mix(mixAmounts, step(mixAmounts, vec3(noise(vec3((texCoord-vec2(0.5))/Scale/oneTexel - Offset, Seed)))), UseNoise);
    }

    fragColor = vec4(mix(col.rgb, baseCol.rgb, mixAmounts), 1.0);
}
