#version 330

uniform sampler2D InSampler;
uniform sampler2D ASampler;
uniform sampler2D BSampler;

layout(std140) uniform ToonConfig {
    float OutlineStep;
    float ColorSteps;
    float ColorBoost;
    float Darkening;
    float Convolution;
};

in vec2 texCoord;

out vec4 fragColor;

float maxV(vec3 v) {
    return max(v.r, max(v.g, v.b));
}

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    vec3 a = texture(ASampler, texCoord).rgb;
    vec3 b = texture(BSampler, texCoord).rgb;

    float difference = maxV(a - b);
    float edge = step(difference, OutlineStep);

    float m = maxV(a);
    color = (color/m)*(round(m*ColorSteps)/ColorSteps);
    color = mix(color, color*color, Darkening)*ColorBoost;

    vec3 convolved = vec3(color.r-(color.g+color.b)*Convolution, color.g-(color.r+color.b)*Convolution, color.b-(color.g+color.r)*Convolution);
    color = max(color, convolved);

    color *= edge;

    fragColor = vec4(color, 1.0);
}
