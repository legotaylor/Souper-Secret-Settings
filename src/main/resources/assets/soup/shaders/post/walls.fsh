#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Scale;
uniform vec2 DepthMix;
uniform vec4 TextureScale;

uniform vec4 FloorUV;
uniform vec4 WallUV;
uniform vec4 CeilUV;

uniform float luminance_alpha_smooth;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main(){
    vec2 samplePos;
    vec4 uv;
    vec2 scale;

    float horizonPosition = texCoord.y*2.0 - 1.0;
    float depth = LinearizeDepth(texture(InDepthSampler, vec2(texCoord.x, mix(DepthMix.y, texCoord.y, DepthMix.x))).r);
    float height = Scale/depth;
    if (abs(horizonPosition) < height) {
        samplePos = vec2((texCoord.x-0.5)*depth, ((horizonPosition/height)+1)/2);
        scale = TextureScale.xy;
        uv = WallUV;
    } else {
        samplePos = vec2(Scale*(texCoord.x-0.5)/abs(horizonPosition), 1/horizonPosition);
        scale = TextureScale.zw;
        uv = horizonPosition < 0 ? FloorUV : CeilUV;
    }

    samplePos = fract(samplePos*scale+vec2(0.5, 0));
    vec4 col = texture(InSampler, vec2(mix(uv.x, uv.z, samplePos.x), mix(uv.y, uv.w, samplePos.y)));

    fragColor = vec4(mix(texture(InSampler, texCoord), col, luminance_alpha_smooth).rgb, 1.0);
}
