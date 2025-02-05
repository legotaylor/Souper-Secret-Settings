#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int soup_shader_index;

void main(){
    vec4 color = texture(PrevSampler, texCoord);
    int channel = soup_shader_index%3;
    if (channel == 0) {
        color.r = 0;
    } else if (channel == 1) {
        color.g = 0;
    } else {
        color.b = 0;
    }
    color += texture(InSampler, texCoord);
    fragColor = vec4(color.rgb, 1);
}