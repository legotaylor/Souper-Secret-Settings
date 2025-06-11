#version 150

uniform sampler2D InSampler;
uniform sampler2D BaseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Swap;
uniform vec4 R;
uniform vec4 G;
uniform vec4 B;
uniform vec3 Abs;
uniform vec3 MinThreshold;
uniform vec3 MinA;
uniform vec3 MinB;
uniform vec3 BaseA;
uniform vec3 BaseB;
uniform vec3 Out;

void main() {
    vec3 colA = texture(InSampler, texCoord).rgb;
    vec3 colB = texture(BaseSampler, texCoord).rgb;

    vec3 s = sin(Swap*1.57079632679);
    vec3 c = cos(Swap*1.57079632679);
    vec3 a = abs(colA * c - colB * s);
    vec3 b = abs(colA * s + colB * c);

    vec3 col = vec3(dot(R.xy*vec2(a.y, -a.z), R.zw+b.zy), dot(G.xy*vec2(a.z, -a.x), G.zw+b.xz), dot(B.xy*vec2(a.x, -a.y), B.zw+b.yx));

    vec3 absCol = abs(col);
    col = mix(col, absCol, Abs);

    col = max(col, max((MinThreshold-absCol)/MinThreshold,0)*(colA*MinA + colB*MinB));

    col = a*BaseA + b*BaseB + clamp(col, 0, 1)*Out;

    fragColor = vec4(col, 1.0);
}
