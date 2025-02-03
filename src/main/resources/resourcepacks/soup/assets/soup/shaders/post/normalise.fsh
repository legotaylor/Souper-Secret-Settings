#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Levels;

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    float m = max(max(col.r,col.g),col.b);
    vec3 normalised = m == 0 ? col : col/m;

    col = normalised*ceil(m*Levels)/Levels;

    fragColor = vec4(col, 1.0);
}
