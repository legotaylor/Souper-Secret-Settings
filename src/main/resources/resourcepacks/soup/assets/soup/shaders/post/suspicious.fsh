#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Scale;
uniform float luminance_alpha_smooth;

const uniform int XOffset [22] = int[22](1, 0, -1, -2, 1, 1, -1, -1, 2, 1, 0, -1, 2, 1, 1, 0, -1, 1, 0, -1, -1, -2);
const uniform int YOffset [22] = int[22](-1, -1, -1, -1, -2, 2, -2, 2, 1, 1, 1, 1, 0, 0, 1, 1, 1, -1, -1, -1, 0, 0);

void main(){
    vec2 pos = floor(texCoord/oneTexel / Scale);

    int i = int(mod(pos.x + pos.y*4, 22.0));

    vec4 col = texture(InSampler, (pos*oneTexel + oneTexel*vec2(XOffset[i], YOffset[i]))*Scale);

    fragColor = vec4(mix(texture(InSampler, texCoord), col, luminance_alpha_smooth).rgb, 1.0);
}
