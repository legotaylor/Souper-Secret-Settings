#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float Radius;
uniform float InnerPercent;
uniform float luminance_alpha_smooth;

out vec4 fragColor;

void main(){
    vec4 base = texture(InSampler, texCoord);

    vec4 maxVal = InnerPercent > 0 ? vec4(0) : base;
    for(float u = 0.0; u <= Radius; u += 1.0) {
        for(float v = u == 0 ? 1.0 : 0.0; v <= Radius; v += 1.0) {
            float d = sqrt(u * u + v * v) / (Radius);
            if (d > 1.0 || d < InnerPercent) continue;

            vec4 s0 = texture(InSampler, texCoord + vec2(-u * oneTexel.x, -v * oneTexel.y));
            vec4 s1 = texture(InSampler, texCoord + vec2( u * oneTexel.x,  v * oneTexel.y));
            vec4 s2 = texture(InSampler, texCoord + vec2(-u * oneTexel.x,  v * oneTexel.y));
            vec4 s3 = texture(InSampler, texCoord + vec2( u * oneTexel.x, -v * oneTexel.y));

            maxVal = max(maxVal, max(max(s0, s1), max(s2, s3)));
        }
    }

    fragColor = vec4(mix(base, maxVal, luminance_alpha_smooth).rgb, 1.0);
}
