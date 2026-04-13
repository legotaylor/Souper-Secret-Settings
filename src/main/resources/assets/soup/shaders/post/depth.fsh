#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D BaseSampler;

layout(std140) uniform DepthConfig {
    uniform float Scale;
    uniform float Offset;
    uniform vec2 Clipping;
};

in vec2 texCoord;

out vec4 fragColor;

float LinearizeDepth(float depth) {
    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

void main() {
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float brightness = min((Scale/depth), 1.0);
    float s = (brightness*brightness);

    vec4 col = texture(InSampler, texCoord);
    vec4 baseCol = texture(BaseSampler, texCoord);

    fragColor = vec4(mix(col.rgb, baseCol.rgb, Offset+(s+brightness-s*brightness)), 1.0);
}
