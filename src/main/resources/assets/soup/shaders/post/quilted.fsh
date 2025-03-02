#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform ivec2 Pixels;
uniform vec2 Blur;
uniform vec3 Highlight;
uniform vec3 Exponent;
uniform vec2 Center;
uniform vec4 Base;
uniform float luminance_alpha_smooth;

void main(){
    vec3 base = texture(InSampler, texCoord).rgb;

    vec2 scale = oneTexel*Pixels;
    vec2 start = floor(texCoord/scale + vec2(0.5))*scale;

    vec3 exponent = 1.0/Exponent;

    vec3 lastCol = vec3(0.0);
    vec3 average = vec3(0.0);
    vec3 difference = vec3(0.0);

    float mag = max(abs(Blur.x), abs(Blur.y));
    int s = -int(floor(mag/2.0));
    int e = int(ceil(mag/2.0));
    for (int i = s; i < e; i++) {
        vec3 col = texture(InSampler, start+(oneTexel*Blur*(i/mag))).rgb;
        average += col;
        vec3 diff = pow(max(col-lastCol, 0), exponent);
        if (i > s) {
            difference += diff;
        } else {
            difference += diff*Highlight.y;
        }
        lastCol = col;
    }

    mag = e-s;
    average = mix(average/mag, Base.rgb, Base.a);
    difference *= Highlight.x/max(mag-1, 1);

    vec3 col = average + (difference+Highlight.z)*length((texCoord-start - Center*scale)/oneTexel);

    fragColor = vec4(mix(base, col, luminance_alpha_smooth), 1.0);
}
