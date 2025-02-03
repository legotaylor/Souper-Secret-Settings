#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform float Scale;
uniform ivec4 Char1A;
uniform ivec4 Char1B;
uniform ivec4 Char2A;
uniform ivec4 Char2B;
uniform ivec4 Char3A;
uniform ivec4 Char3B;
uniform ivec4 Char4A;
uniform ivec4 Char4B;
uniform ivec4 Char5A;
uniform ivec4 Char5B;

bool getChar(ivec4 charA, ivec4 charB, ivec2 index) {
    return ((index.y > 3 ? charB : charA)[index.y%4] & (1 << index.x)) > 0;
}

void main() {
    ivec2 pixelCoord = ivec2(texCoord/oneTexel/Scale);

    vec3 col = texture(InSampler, (pixelCoord/8)*8*oneTexel*Scale).rgb;

    float l = dot(col, vec3(0.3, 0.59, 0.11));

    ivec4 charA;
    ivec4 charB;
    if (l < 0.2) {
        charA = Char1A;
        charB = Char1B;
    } else if (l < 0.4) {
        charA = Char2A;
        charB = Char2B;
    } else if (l < 0.6) {
        charA = Char3A;
        charB = Char3B;
    } else if (l < 0.8) {
        charA = Char4A;
        charB = Char4B;
    } else {
        charA = Char5A;
        charB = Char5B;
    }

    col *= getChar(charA, charB, ivec2(7)-(pixelCoord)%8) ? 1 : 0;

    fragColor = vec4(col, 1.0);
}
