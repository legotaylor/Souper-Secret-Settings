#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Steps;
uniform float BaseMix;
uniform vec2 RotationScale;
uniform vec4 Offset;
uniform float luminance_fov;
uniform float luminance_pitch;
uniform float luminance_pitch_delta;
uniform float luminance_yaw_delta;
uniform float luminance_alpha_smooth;

mat3 pitch(float rotation) {
    rotation /= 57.2957795131;
    float s = sin(rotation);
    float c = cos(rotation);
    return mat3(1, 0, 0, 0, c, -s, 0, s, c);
}

mat3 yaw(float rotation) {
    rotation /= 57.2957795131;
    float s = sin(rotation);
    float c = cos(rotation);
    return mat3(c, 0, s, 0, 1, 0, -s, 0, c);
}

void main(){
    float near = 0.1;
    float far = 1000.0;
    float aspect = oneTexel.y/oneTexel.x;
    float yTan = tan(luminance_fov/114.591559);
    float yCotan = 1.0/yTan;
    mat4 projection = mat4(yCotan/aspect, 0, 0, 0, 0, yCotan, 0, 0, 0, 0, (far+near)/(near-far), (2*far*near)/(near-far), 0, 0, -1, 0);

    vec3 pos = (vec3(yTan * (texCoord.x*2.0 - 1.0) * aspect, yTan * (texCoord.y*2.0 - 1.0), -Offset.w) + Offset.xyz);
    pos *= pitch(-luminance_pitch);

    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 colAcc = base * BaseMix;

    mat3 pitchCorrection = pitch(luminance_pitch);
    mat3 pitchStep = pitch(luminance_pitch_delta/Steps * RotationScale.x);
    float yawStep = luminance_yaw_delta/Steps * RotationScale.y;
    float count = floor(abs(Steps));
    for (int i = 1; i < count; i++) {
        pos *= pitchStep;
        vec4 projected = vec4(pos * (yaw(yawStep * i) * pitchCorrection), 0.0) * projection;
        vec2 uv = ((projected.xy / projected.z) + vec2(1.0)) / 2;

        colAcc += texture(InSampler, uv).rgb;
    }

    colAcc /= count - (1-BaseMix);

    fragColor = vec4(mix(base, colAcc, luminance_alpha_smooth), 1.0);
}
