#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform float Iterations;
uniform vec2 Scale;

const float roundingFactor = 0.5/255.0;

//mimics the way floats are converted into 8 bit fixed point
float limit(float f) {
    f = clamp(f,0,1);
    float i = f >= 0.5 ? -roundingFactor : roundingFactor;
    return round(255.0*(f - (mod(i - f, 1.0/16.0) - i) / 256.0))/255.0;
}

vec4 limit(vec4 v) {
    return vec4(limit(v.x), limit(v.y), limit(v.z), v.w);
}

void main(){
    vec4 col = texture(InSampler, texCoord);

    for (int i = 0; i < Iterations; i++) {
        col = limit(abs(col - vec4(Scale.x)) * Scale.y);
    }

    fragColor = vec4(col.rgb, 1.0);
}
