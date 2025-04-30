#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Scale;
uniform float Offset;
uniform vec2 luminance_clipping;

float LinearizeDepth(float depth) {
    return (luminance_clipping.x*luminance_clipping.y) / (depth * (luminance_clipping.x - luminance_clipping.y) + luminance_clipping.y);
}

void main() {
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float brightness = min((Scale/depth), 1.0);
    float s = (brightness*brightness);

    vec4 col = texture(InSampler, texCoord);
    vec4 baseCol = texture(BaseSampler, texCoord);

    fragColor = vec4(mix(col.rgb, baseCol.rgb, Offset+(s+brightness-s*brightness)), 1.0);
}
