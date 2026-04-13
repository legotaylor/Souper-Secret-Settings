#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 InDepthSize;
};

layout(std140) uniform CameraShiftConfig {
    vec3 Offset;
    vec3 Rotation;
    vec3 Camera;

    float Fov;
    float ZStep;
    float ZGrowth;
    float Steps;
    float SubThreshold;
    float SubSteps;

    float Wrapping;
    vec2 Clipping;
};

in vec2 texCoord;

out vec4 fragColor;

vec2 wrapCoord(vec2 coord) {
    return mix(coord, fract(coord), Wrapping);
}

float LinearizeDepth(float depth) {
    // treat the hand as at the far plane, so it doesnt cause issues
    if (depth == 0.0) {
        return Clipping.y;
    }

    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

vec2 GetRayPos(mat4 projection, mat4 coord, float xSlope, float ySlope, float d) {
    vec4 pos = vec4(xSlope * d, (ySlope * d), -d, 1.0);

    pos = coord*pos;

    pos.w = 0;
    pos = projection*pos;
    return ((pos.xy/pos.z) + vec2(1.0))/2;
}

vec2 ExponentialRaycast(mat4 projection, mat4 coord, float xSlope, float ySlope) {
    vec2 screen;
    float zStep = ZStep;
    float i;
    float d;
    float depth;

    for (i = 1; i < Steps; i++) {
        //d = ZStep * (ZGrowth^i) * i
        d = i*zStep;

        screen = GetRayPos(projection, coord, xSlope, ySlope, d);
        depth = LinearizeDepth(texture(InDepthSampler, wrapCoord(screen)).r);

        if (depth < d) {
            break;
        }

        zStep *= ZGrowth;
    }

    if (depth < SubThreshold) {
        float start = i * zStep / ZGrowth;

        for (i = 0; i < SubSteps; i++) {
            float t = i / SubSteps;

            float d2 = mix(start, d, t);
            screen = GetRayPos(projection, coord, xSlope, ySlope, d2);
            depth = LinearizeDepth(texture(InDepthSampler, wrapCoord(screen)).r);

            if (depth < d2) {
                break;
            }
        }
    }

    return screen;
}

mat4 GetRotationMatrix(vec3 rotation) {
    rotation *= 6.28318530718;
    float sx = sin(rotation.x);
    float cx = cos(rotation.x);
    float sy = sin(rotation.y);
    float cy = cos(rotation.y);
    float sz = sin(rotation.z);
    float cz = cos(rotation.z);

    // im pretty sure the compiler simplifies this
    return transpose(mat4(1,  0,   0,  0,     0,  cx, -sx, 0,     0,  sx, cx, 0,      0, 0, 0, 1) *
                     mat4(cy, 0,   sy, 0,     0,  1,  0,   0,    -sy, 0,  cy, 0,      0, 0, 0, 1) *
                     mat4(cz, -sz, 0,  0,     sz, cz, 0,   0,     0,  0,  1,  0,      0, 0, 0, 1));
}

void main(){
    float aspect = InSize.y/InSize.x;

    float yTan = tan(Fov/114.591559);
    float yCotan = 1.0/yTan;

    float xSlope = aspect*yTan * (texCoord.x*2.0 - 1.0);
    float ySlope = yTan * (texCoord.y*2.0 - 1.0);

    //https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/gluPerspective.xml
    mat4 projection = mat4(yCotan/aspect, 0, 0, 0, 0, yCotan, 0, 0, 0, 0, (Clipping.y+Clipping.x)/(Clipping.x-Clipping.y), (2*Clipping.y*Clipping.x)/(Clipping.x-Clipping.y), 0, 0, -1, 0);

    mat4 coord = GetRotationMatrix(Rotation) * mat4(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  Offset.x, Offset.y, Offset.z, 1) * GetRotationMatrix(Camera);

    vec2 hitPos = ExponentialRaycast(projection, coord, xSlope, ySlope);
    vec4 color = texture(InSampler, wrapCoord(hitPos));

    fragColor = vec4(color.rgb, 1.0);
}
