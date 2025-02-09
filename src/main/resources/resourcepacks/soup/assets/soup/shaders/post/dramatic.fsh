#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Boost;
uniform float Clamp;
uniform vec3 Gray;
uniform float luminance_alpha_smooth;

void main(){
    vec4 col = texture(InSampler, texCoord);
    
    float scale = min((col.r*Gray.r + col.g*Gray.g + col.b*Gray.b)*Boost, Clamp);

    fragColor = vec4(mix(col.rgb, col.rgb * scale, luminance_alpha_smooth), 1.0);
}
