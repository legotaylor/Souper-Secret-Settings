#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform TextureMapConfig {
    float Fov;
    float Pitch;
    float Yaw;
    vec3 CamFract;
    vec3 Cam;
    vec2 Clipping;
    float TextureScale;
    vec3 Offset;
    float Aspect;
    vec3 Coloring;
    vec2 UVMix;
    vec4 UVDistances;
};

in vec2 texCoord;

out vec4 fragColor;

float LinearizeDepth(float depth) {
    return (Clipping.x*Clipping.y) / (depth * (Clipping.x - Clipping.y) + Clipping.y);
}

float aspect = InSize.x/InSize.y;
float yTan = tan(Fov/114.591559);

mat3 GetRotationMatrix(vec2 rotation) {
    rotation /= 57.2957795131;
    float sx = sin(rotation.x);
    float cx = cos(rotation.x);
    float sy = sin(rotation.y);
    float cy = cos(rotation.y);
    return transpose(mat3(cy, 0, sy, 0, 1, 0, -sy, 0, cy) * mat3(1, 0, 0, 0, cx, -sx, 0, sx, cx));
}

mat3 rotation = GetRotationMatrix(vec2(Pitch, Yaw));

float GetDepth(vec2 coord) {
    return LinearizeDepth(texture(InDepthSampler, coord).r);
}

vec3 GetWorldOffset(vec2 coord, float d) {
    float xSlope = yTan * (coord.x*2.0 - 1.0) * aspect;
    float ySlope = yTan * (coord.y*2.0 - 1.0);

    vec3 pos = vec3(xSlope * d, (ySlope * d), -d) + Offset;
    return (pos * rotation);
}

vec3 GetWorldOffset(vec2 coord) {
    return GetWorldOffset(coord, GetDepth(coord));
}

void main(){
    vec2 oneTexel = 1.0 / InSize;
    float dist = GetDepth(texCoord);
    vec3 pos = GetWorldOffset(texCoord, dist);
    vec3 offsetLeft =  pos - GetWorldOffset(texCoord + vec2(-oneTexel.x * UVDistances.z, 0));
    vec3 offsetRight = pos - GetWorldOffset(texCoord + vec2( oneTexel.x * UVDistances.x, 0));
    vec3 offsetUp =    pos - GetWorldOffset(texCoord + vec2(0,  oneTexel.y * UVDistances.y));
    vec3 offsetDown =  pos - GetWorldOffset(texCoord + vec2(0, -oneTexel.y * UVDistances.w));

    vec3 totalOffset = abs(offsetLeft) + abs(offsetRight) + abs(offsetUp) + abs(offsetDown);

    vec3 flip = offsetLeft - offsetRight + offsetUp - offsetDown;
    flip.y = pos.y;

    vec3 offset = CamFract/TextureScale + fract(floor(Cam)/TextureScale);
    offset.y = -offset.y;
    pos = fract(pos/TextureScale - offset);

    vec2 uv;
    if (totalOffset.x < totalOffset.y && totalOffset.x < totalOffset.z) {
        uv = pos.zy;
        if (flip.z < 0) {
            uv.x = 1-uv.x;
        }
    } else if (totalOffset.y < totalOffset.x && totalOffset.y < totalOffset.z) {
        uv = pos.xz;
        if (flip.y > 0) {
            uv = vec2(1)-uv;
        }
    } else {
        uv = pos.xy;
        if (flip.x < 0) {
            uv.x = 1-uv.x;
        }
    }

    uv = mix(texCoord, mix(uv, ((uv-vec2(0.5))/vec2(aspect, 1.0))+vec2(0.5), Aspect), dist < UVMix.y ? UVMix.x : 0.0);
    vec3 color = texture(InSampler, uv).rgb;
    color = mix(color, color*mix(totalOffset, normalize(totalOffset), Coloring.y), Coloring.x);
    color = mix(color, color*pos, Coloring.z);
    //returning totalOffset or normalise(totalOffset) as the color looks very cool

    fragColor = vec4(color, 1.0);
}
