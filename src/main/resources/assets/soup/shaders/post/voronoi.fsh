#version 150

uniform sampler2D InSampler;
uniform sampler2D PrevOutSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

out vec4 fragColor;

uniform float Last;
uniform float Adjacent;
uniform float Diagonal;
uniform vec4 SampleDistance;

void main() {
    vec4 col = texture(InSampler, texCoord);

    vec2 aTexel = oneTexel*SampleDistance.xy;
    vec2 bTexel = oneTexel*SampleDistance.z;
    vec2 cTexel = oneTexel*SampleDistance.w;

    vec4 c = texture(PrevOutSampler, texCoord);
    vec4 u = texture(PrevOutSampler, texCoord + vec2(        0.0, -aTexel.y));
    vec4 d = texture(PrevOutSampler, texCoord + vec2( aTexel.x,         0.0));
    vec4 l = texture(PrevOutSampler, texCoord + vec2(-aTexel.x,         0.0));
    vec4 r = texture(PrevOutSampler, texCoord + vec2(        0.0,  aTexel.y));

    vec4 ul = texture(PrevOutSampler, texCoord + vec2(-cTexel.x,  cTexel.y));
    vec4 dl = texture(PrevOutSampler, texCoord + vec2(-bTexel.x, -bTexel.y));
    vec4 ur = texture(PrevOutSampler, texCoord + vec2( bTexel.x,  bTexel.y));
    vec4 dr = texture(PrevOutSampler, texCoord + vec2( cTexel.x, -cTexel.y));

    vec4 d1 = max(max(u, d), max(l, r));
    vec4 d2 = max(max(ul, dl), max(ur, dr));
    vec4 m = max(max(c, d1*Adjacent), (d1+d2)*Diagonal);
    
    m=min(m-(0.5/255), m*Last);

    fragColor = vec4(max(m,col).rgb, 1.0);
}
