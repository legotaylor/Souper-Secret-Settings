#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Scale;
uniform vec2 Offset;
uniform float Pixelate;
uniform float Seed;
uniform vec2 Step;
uniform vec3 Cycle;
uniform float luminance_time;

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec4 col = texture(InSampler, texCoord);
    vec4 prevCol = texture(PrevSampler, texCoord);

    vec2 noiseCoord = (texCoord-vec2(0.5))/oneTexel;
    if (Pixelate != 0) {
        noiseCoord = floor(noiseCoord/Pixelate)*Pixelate;
    }

    vec3 m = vec3(hash(vec3(noiseCoord/Scale - Offset, Seed)));
    m = fract(m + Cycle + vec3(luminance_time));
    m = mix(m , step(Step.x, m), Step.y);

    fragColor = vec4(mix(col.rgb, prevCol.rgb, m), 1.0);
}
