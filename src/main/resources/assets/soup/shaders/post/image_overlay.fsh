#version 330

uniform sampler2D InSampler;
uniform sampler2D OverlaySampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 OverlaySize;
};

layout(std140) uniform ImageOverlayConfig {
    vec2 Position;
    vec2 Scale;
    vec4 Color;
    vec4 DropShadow;
    vec3 Strength;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;

    vec2 size = OverlaySize;
    vec2 pos = floor((floor(texCoord*InSize) - Position*InSize)/Scale + floor(Position*size));
    bool inside = pos.x >= 0 && pos.x < size.x && pos.y >= 0 && pos.y < size.y;
    pos /= size-vec2(1.0);

    if (inside) {
        pos.y = 1-pos.y;
        vec4 image = texture(OverlaySampler, pos)*Color;
        col = mix(col, image.rgb, image.a);
    } else {
        size *= Scale;
        pos *= size;
        pos -= DropShadow.xy + size/2.0;
        col = mix(col, col*min(length(max(abs(pos*2) - abs(size) - vec2(DropShadow.z), 0))/DropShadow.w, 1), Strength);
    }

    fragColor = vec4(col, 1.0);
}
