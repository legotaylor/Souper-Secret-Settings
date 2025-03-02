#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float luminance_alpha_smooth;

void main(){
    vec4 col = texture(InSampler, texCoord);

    vec4 contrast = col*col*(3.0 - 2.0*col);

    fragColor = vec4(mix(col, contrast, luminance_alpha_smooth).rgb, 1.0);
}
