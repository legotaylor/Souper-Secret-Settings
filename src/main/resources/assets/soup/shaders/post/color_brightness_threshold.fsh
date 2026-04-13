#version 330

uniform sampler2D InSampler;

layout(std140) uniform ThresholdConfig {
    float ThresholdBrightness;
    float ThresholdSlope;
    vec3 Luminance;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 color = texture(InSampler, texCoord);
    float luminance = dot(Luminance, color.rgb);
    if (luminance > ThresholdBrightness) {
        fragColor = (color/max(max(color.r, color.g), color.b))*min(((luminance-ThresholdBrightness)/(1.0-ThresholdBrightness)*ThresholdSlope), 1.0);
    } else {
        fragColor = vec4(0.0);
    }
}
