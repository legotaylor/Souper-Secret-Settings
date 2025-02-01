#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 RGBInfluence;
uniform vec3 RGBLoop;
uniform vec3 HSVInfluence;
uniform vec3 HSVLoop;

uniform float Flip;
uniform float SortThreshold;

float ThresholdValue(float value) {
    float stepValue = step(abs(SortThreshold), value);
    return SortThreshold == 0 ? 1 : (SortThreshold > 0 ? stepValue : 1-stepValue);
}

vec3 RGBtoHSV(vec3 rgb) {
    vec3 hsv = vec3(0.0);
    hsv.z = max(rgb.r, max(rgb.g, rgb.b));
    float min = min(rgb.r, min(rgb.g, rgb.b));
    float c = hsv.z - min;

    if (c != 0.0)
    {
        hsv.y = c / hsv.z;
        vec3 delta = (hsv.z - rgb) / c;
        delta.rgb -= delta.brg;
        delta.rg += vec2(2.0, 4.0);
        if (rgb.r >= hsv.z) {
            hsv.x = delta.b;
        } else if (rgb.g >= hsv.z) {
            hsv.x = delta.r;
        } else {
            hsv.x = delta.g;
        }
        hsv.x = fract(hsv.x / 6.0);
    }
    return hsv;
}

float Influence(vec3 vec, vec3 influence, vec3 loop) {
    return fract(vec.x*influence.x+loop.x) + fract(vec.y*influence.y+loop.y) + fract(vec.z*influence.z+loop.z);
}

void main(){
    vec3 color = texture(InSampler, texCoord).rgb;
    float value = (Influence(color, RGBInfluence, RGBLoop) + Influence(RGBtoHSV(color), HSVInfluence, HSVLoop));
    float t = ThresholdValue(value);
    value *= (0.5 - Flip) * 32.0;
    fragColor = vec4(vec3(fract(value), floor(value+127.0)/255.0, t), 1.0);
}
