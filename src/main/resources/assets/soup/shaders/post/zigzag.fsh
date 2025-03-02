#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Zig;
uniform vec3 Zag;
uniform vec3 Curve;
uniform float Wrapping;
uniform float luminance_alpha_smooth;

vec4 wrapTexture(sampler2D tex, vec2 coord) {
    return texture(tex, mix(coord, fract(coord), Wrapping));
}

float bounce(float t) {
    t = pow(fract(t), Curve.z)-Curve.x;
    return pow(t / (step(0, t)-Curve.x), Curve.y);
}

void main() {
    vec2 coord = texCoord + Zag.xy*(bounce(dot(texCoord-vec2(0.5), Zig.xy) + Zig.z)-Zag.z);
    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, wrapTexture(InSampler, coord).rgb, luminance_alpha_smooth), 1.0);
}
