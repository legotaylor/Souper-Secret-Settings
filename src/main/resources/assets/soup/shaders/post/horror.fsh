#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Scale;
uniform vec2 luminance_clipping;
uniform float luminance_alpha_smooth;

float LinearizeDepth(float depth) {
    return (luminance_clipping.x*luminance_clipping.y) / (depth * (luminance_clipping.x - luminance_clipping.y) + luminance_clipping.y);
}

void main(){
    vec4 col = texture(InSampler, texCoord);
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float brightness = min((Scale/depth), 1.0);
    float s = (brightness*brightness);
    fragColor = vec4(col.rgb*(1-(1-(s+brightness-s*brightness))*luminance_alpha_smooth), 1.0);
}
