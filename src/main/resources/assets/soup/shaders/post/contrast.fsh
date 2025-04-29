#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Amount;
uniform vec3 Power;
uniform float luminance_alpha_smooth;

void main(){
    vec3 col = texture(InSampler, texCoord).rgb;

    vec3 value = mix(col, pow(col, Power) * (3.0 - 2.0*col), Amount);

    fragColor = vec4(mix(col, clamp(value, 0.0, 1.0), luminance_alpha_smooth), 1.0);
}
