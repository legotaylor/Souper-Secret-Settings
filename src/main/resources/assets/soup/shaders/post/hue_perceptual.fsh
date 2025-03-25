#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float Rotation;
uniform vec3 Gray;
uniform vec3 R;
uniform vec3 G;
uniform vec3 B;
uniform float luminance_alpha_smooth;

out vec4 fragColor;

// https://gist.github.com/mairod/a75e7b44f68110e1576d77419d608786
vec3 hue_shift(vec3 color, float dhue) {
    float s = sin(dhue);
    float c = cos(dhue);
    return (color * c) + (color * s) * mat3(R, G, B) + dot(Gray, color) * (1.0 - c);
}

void main(){
    vec4 col = texture(InSampler, texCoord);
    fragColor = vec4(mix(col.rgb, clamp(hue_shift(col.rgb, Rotation * -6.28318530718), 0, 1), luminance_alpha_smooth), 1.0);
}
