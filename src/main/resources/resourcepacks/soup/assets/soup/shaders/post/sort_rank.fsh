#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Gray;

void main(){
    vec3 color = texture(InSampler, texCoord).rgb;
    fragColor = vec4(vec3(dot(color, Gray)), 1.0);
}
