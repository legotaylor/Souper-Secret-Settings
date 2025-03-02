#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Levels;
uniform float luminance_alpha_smooth;

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    float m = max(max(col.r,col.g),col.b);
    vec3 normalised = m == 0 ? col : col/m;

    fragColor = vec4(mix(col, normalised*ceil(m*Levels)/Levels, luminance_alpha_smooth), 1.0);
}
