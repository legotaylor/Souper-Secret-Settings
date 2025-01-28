#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int soup_shader_index;

void main(){
    vec4 color = texture(soup_shader_index%3 == 0 ? InSampler : PrevSampler, texCoord);
    fragColor = vec4(color.rgb, 1);
}