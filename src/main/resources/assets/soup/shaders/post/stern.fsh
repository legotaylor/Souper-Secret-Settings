#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform mat2x2 Left;
uniform mat2x2 Right;
uniform ivec4 Bitshifts;
uniform vec2 Start;
uniform vec2 Range;
uniform float luminance_alpha_smooth;

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;
    
    ivec3 bits = ivec3(base*255.0);
    vec2 r = Start;
    vec2 g = Start;
    vec2 b = Start;

    for (int i = Bitshifts.x; i < Bitshifts.y; i++) {
        ivec3 layer = bits & (Bitshifts.z << i);

        r = layer.r > Bitshifts.w ? r*Right : r*Left;
        g = layer.g > Bitshifts.w ? g*Right : g*Left;
        b = layer.b > Bitshifts.w ? b*Right : b*Left;
    }

    vec3 col = (vec3(r.x/r.y, g.x/g.y, b.x/b.y) + Range.x) * Range.y;

    fragColor = vec4(mix(base, clamp(col,0,1), luminance_alpha_smooth), 1.0);
}
