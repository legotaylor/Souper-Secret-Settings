#version 150

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 ColorScale;
uniform float Mix;
uniform vec2 luminance_clipping;
uniform float luminance_alpha_smooth;

float LinearizeDepth(float depth) {
    return (luminance_clipping.x*luminance_clipping.y) / (depth * (luminance_clipping.x - luminance_clipping.y) + luminance_clipping.y);
}

void main(){
    vec4 col = texture(InSampler, texCoord);

    float depth = LinearizeDepth(texture(DepthSampler, texCoord).r);
    float depthUp = LinearizeDepth(texture(DepthSampler, texCoord+vec2(0.0, oneTexel.y)).r);

    float diff = abs(depth-depthUp);
    col = mix(col, clamp(col * vec4(ColorScale*diff, 1.0), 0, 1), luminance_alpha_smooth);

    fragColor = vec4(col.rgb, 1.0);
}
