#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Scale;
uniform float luminance_alpha_smooth;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

void main(){
    vec4 col = texture(InSampler, texCoord);
    float depth = LinearizeDepth(texture(InDepthSampler, texCoord).r);
    float brightness = min((Scale/depth), 1.0);
    float s = (brightness*brightness);
    fragColor = vec4(col.rgb*(1-(1-(s+brightness-s*brightness))*luminance_alpha_smooth), 1.0);
}
