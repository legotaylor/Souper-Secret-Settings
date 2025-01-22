#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Offset;

uniform float YFov;
uniform float ZStep;
uniform float ZGrowth;
uniform float Steps;
uniform float SubThreshold;
uniform float SubSteps;

float near = 0.1;
float far = 1000.0;
float LinearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0;
    return (near * far) / (far + near - z * (far - near));
}

vec2 GetRayPos(mat4 projection, float xSlope, float ySlope, vec3 offset, float d) {
    vec3 pos = vec3(xSlope * d, (ySlope * d), -d) + offset;

    vec4 projected = projection*vec4(pos, 0.0);
    return ((projected.xy/projected.z) + vec2(1.0))/2;
}

vec2 SubStepRaycast(mat4 projection, float xSlope, float ySlope, vec3 offset, float start, float end) {
    vec2 screen;
    for (float i = 0; i < SubSteps; i++) {
        float t = i/SubSteps;

        float d = mix(start, end, t);
        screen = GetRayPos(projection, xSlope, ySlope, offset, d);
        float depth = LinearizeDepth(texture(InDepthSampler, screen).r);

        if (depth < d) {
            return screen;
        }
    }
    return screen;
}

vec2 ExponentialRaycast(mat4 projection, float xSlope, float ySlope, vec3 offset) {
    vec2 screen;
    float zStep = ZStep;
    for (float i = 1; i < Steps; i++) {
        //d = ZStep * (ZGrowth^i) * i
        float d = i*zStep;

        screen = GetRayPos(projection, xSlope, ySlope, offset, d);
        float depth = LinearizeDepth(texture(InDepthSampler, screen).r);

        if (depth < d) {
            if (depth < SubThreshold) {
                return SubStepRaycast(projection, xSlope, ySlope, offset, i * zStep/ZGrowth, d);
            }
            return screen;
        }
        zStep *= ZGrowth;
    }
    return screen;
}

void main(){
    float aspect = oneTexel.x/oneTexel.y;

    // convert = 57.2958 // convert*2 = 114.591559
    // XFov = atan(aspect*(YFov/90))*convert*2
    // xSlope = tan(XFov/2.0 / convert) * ...
    // this simplifies, so the calculation for xSlope looks different to ySlope

    float yTan = tan(YFov/114.591559);
    float yCotan = 1.0/yTan;

    float ySlope = yTan * (texCoord.y*2.0 - 1.0);
    float xSlope = aspect*(YFov/90) * (texCoord.x*2.0 - 1.0);

    //https://registry.khronos.org/OpenGL-Refpages/gl2.1/xhtml/gluPerspective.xml
    mat4 projection = mat4(yCotan/aspect, 0, 0, 0, 0, yCotan, 0, 0, 0, 0, (far+near)/(near-far), (2*far*near)/(near-far), 0, 0, -1, 0);

    vec2 hitPos = ExponentialRaycast(projection, xSlope, ySlope, Offset);
    vec4 color = texture(InSampler, hitPos);

    fragColor = vec4(color.rgb, 1.0);
}
