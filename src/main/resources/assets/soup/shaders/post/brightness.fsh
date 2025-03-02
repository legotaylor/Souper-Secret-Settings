#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float luminance_alpha_smooth;
uniform vec3 Gray;

void main(){
    vec4 col = texture(InSampler, texCoord);
    
    col = mix(col, 1.0 - (1.0-col)*(1.0-col), luminance_alpha_smooth);

    fragColor = vec4(col.rgb, 1.0);
}
