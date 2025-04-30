#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec4 Mode;
uniform float Colored;
uniform vec2 NoiseCurve;
uniform float Loop;
uniform vec3 Scale;
uniform vec3 Offset;
uniform vec3 Scroll;
uniform float luminance_fov;
uniform float luminance_pitch;
uniform float luminance_yaw;
uniform vec3 luminance_cam_fract;
uniform vec3 luminance_cam;
uniform vec2 luminance_clipping;
uniform float luminance_alpha_smooth;

float LinearizeDepth(float depth) {
    return (luminance_clipping.x*luminance_clipping.y) / (depth * (luminance_clipping.x - luminance_clipping.y) + luminance_clipping.y);
}

float aspect = oneTexel.y/oneTexel.x;
float yTan = tan(luminance_fov/114.591559);

mat3 GetRotationMatrix(vec2 rotation) {
    rotation /= 57.2957795131;
    float sx = sin(rotation.x);
    float cx = cos(rotation.x);
    float sy = sin(rotation.y);
    float cy = cos(rotation.y);
    return transpose(mat3(cy, 0, sy, 0, 1, 0, -sy, 0, cy) * mat3(1, 0, 0, 0, cx, -sx, 0, sx, cx));
}

mat3 rotation = GetRotationMatrix(vec2(luminance_pitch, luminance_yaw));

float GetDepth(vec2 coord) {
    return LinearizeDepth(texture(InDepthSampler, coord).r);
}

vec3 GetWorldOffset(vec2 coord, float d) {
    float xSlope = yTan * (coord.x*2.0 - 1.0) * aspect;
    float ySlope = yTan * (coord.y*2.0 - 1.0);

    vec3 pos = vec3(xSlope * d, (ySlope * d), -d);
    return (pos * rotation);
}

vec3 GetWorldOffset(vec2 coord) {
    return GetWorldOffset(coord, GetDepth(coord));
}

vec3 edge = Loop*Scale;

//https://www.shadertoy.com/view/3sGSWV

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 rand(vec3 p3){
    vec3 p = p3 - (step(edge, p3) * edge + floor(Scroll));
    vec3 n = vec3(hash(p));
    if (Colored == 0) {
        return n;
    }
    vec3 c = n;
    c.y = hash(n+p);
    c.z = hash(n+c);
    c.x = hash(c);
    return mix(n, c, Colored);
}

// From iq: https://www.shadertoy.com/view/4sfGzS
vec3 noise(vec3 x){
    vec3 i = floor(x);
    vec3 f = fract(x);
    f = mix(f, f*f*(3.0-2.0*f), NoiseCurve.x);
    f = mix(f, clamp(f, 0, 1),  NoiseCurve.y+1);
    return mix(mix(mix(rand(i),
                       rand(i+vec3(1, 0, 0)),f.x),
                   mix(rand(i+vec3(0, 1, 0)),
                       rand(i+vec3(1, 1, 0)),f.x),f.y),
               mix(mix(rand(i+vec3(0, 0, 1)),
                       rand(i+vec3(1, 0, 1)),f.x),
                   mix(rand(i+vec3(0, 1, 1)),
                       rand(i+vec3(1, 1, 1)),f.x),f.y),f.z);
}

void main(){
    float dist = GetDepth(texCoord);
    vec3 pos = GetWorldOffset(texCoord, dist) + Offset - fract(Scroll)/Scale;

    vec3 offset = luminance_cam_fract/Loop + fract(floor(luminance_cam)/Loop);
    offset.y = -offset.y;
    pos = (fract(pos/Loop - offset)*Loop)*Scale;

    vec3 color = texture(InSampler, texCoord).rgb;
    vec3 base = color;

    vec3 n = noise(pos);

    color = mix(color, step(n, pow(color, vec3(Mode.y))), Mode.x);
    color = mix(color, color*mix(n, 1-n, Mode.w), Mode.z);

    fragColor = vec4(mix(base, color, luminance_alpha_smooth), 1.0);
}
