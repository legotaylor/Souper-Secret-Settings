#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float Scale;
uniform vec2 Offset;
uniform vec3 Angle;
uniform float Seed;
uniform float luminance_alpha_smooth;

//https://www.shadertoy.com/view/3sGSWV

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

//https://www.ronja-tutorials.com/post/028-voronoi-noise/

vec2 offset(vec2 cell) {
    return cell + vec2(hash(vec3(cell.x, Seed, -cell.y))-0.5, hash(vec3(cell.y, -cell.x, Seed))-0.5)*Offset;
}

vec2 voronoiNoise(vec2 value){
    vec2 center = floor(value);

    float smallestDistance = 10.0;
    vec2 closestCell;

    for(int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 cellPosition = center + vec2(x,y);
            float dist = length(offset(cellPosition) - value);
            if (dist < smallestDistance) {
                smallestDistance = dist;
                closestCell = cellPosition;
            }
        }
    }

    return closestCell;
}

void main(){
    vec2 scale = (vec2(1.0)/oneTexel)/Scale;

    vec2 pos = texCoord*scale;
    vec2 cell = voronoiNoise(pos);
    vec3 color = texture(InSampler, cell/scale).rgb;

    vec2 slope = (vec2(hash(vec3(cell.x, cell.y, Seed))-0.5, hash(vec3(Seed, cell.x, cell.y))-0.5)*Angle.xy)*pos;
    float angle = fract(slope.x + slope.y);
    color = mix(color, step(angle, color*color), Angle.z);

    fragColor = vec4(mix(texture(InSampler, texCoord).rgb, color, luminance_alpha_smooth), 1);
}