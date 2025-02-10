#version 150

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec4 UVDistances;
uniform float luminance_alpha_smooth;

void main() {
    vec3 pos = texture(InSampler, texCoord).rgb;

    vec3 offsetLeft =  pos - texture(InSampler, texCoord + vec2(-oneTexel.x * UVDistances.z, 0)).rgb;
    vec3 offsetRight = pos - texture(InSampler, texCoord + vec2( oneTexel.x * UVDistances.x, 0)).rgb;
    vec3 offsetUp =    pos - texture(InSampler, texCoord + vec2(0,  oneTexel.y * UVDistances.y)).rgb;
    vec3 offsetDown =  pos - texture(InSampler, texCoord + vec2(0, -oneTexel.y * UVDistances.w)).rgb;

    vec3 totalOffset = abs(offsetLeft) + abs(offsetRight) + abs(offsetUp) + abs(offsetDown);

    vec2 uv;
    if (totalOffset.x < totalOffset.y && totalOffset.x < totalOffset.z) {
        uv = pos.zy;
    } else if (totalOffset.y < totalOffset.x && totalOffset.y < totalOffset.z) {
        uv = pos.xz;
    } else {
        uv = pos.xy;
    }

    fragColor = vec4(mix(pos, texture(BaseSampler, uv).rgb, luminance_alpha_smooth), 1.0);
}
