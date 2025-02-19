#version 150

uniform sampler2D InSampler;
uniform sampler2D OverlaySampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec2 Position;
uniform vec2 Scale;
uniform vec4 DropShadow;
uniform vec3 Strength;

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;

    vec2 size = textureSize(OverlaySampler, 0);
    vec2 pos = floor((floor(texCoord/oneTexel) - Position/oneTexel)/Scale + floor(Position*size));
    bool inside = pos.x >= 0 && pos.x < size.x && pos.y >= 0 && pos.y < size.y;
    pos /= size-vec2(1.0);

    if (inside) {
        pos.y = 1-pos.y;
        vec4 image = texture(OverlaySampler, pos);
        col = mix(col, image.rgb, image.a);
    } else {
        size *= Scale;
        pos *= size;
        pos -= DropShadow.xy + size/2.0;
        col = mix(col, col*min(length(max(abs(pos*2) - abs(size) - vec2(DropShadow.z), 0))/DropShadow.w, 1), Strength);
    }

    fragColor = vec4(col, 1.0);
}
