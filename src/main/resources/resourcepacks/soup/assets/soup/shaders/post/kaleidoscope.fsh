#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform int Iterations;
uniform vec3 LineA;
uniform vec3 LineB;

float distanceToSide(vec2 point, vec3 line) {
    return line.x*point.x+line.y*point.y+line.z;
}

vec2 mirrorAlongLine(vec2 point, vec3 line, float distanceToSide) {
    return point - vec2(line.x, line.y)*(distanceToSide/(line.x*line.x+line.y*line.y))*2;
}

vec2 mirrorAlongLines(vec2 point) {
    for (int i = 0; i < Iterations; i++) {
        vec3 a;
        vec3 b;
        if (i%2 == 0) {
            a = LineA;
            b = LineB;
        } else {
            a = LineB;
            b = LineA;
        }

        float distanceA = distanceToSide(point, a);
        float distanceB = distanceToSide(point, b);
        if (distanceA >= 0 && distanceB >= 0) {
            return point;
        }

        point = mirrorAlongLine(point, a, distanceA);
    }

    return point;
}

void main(){
    vec2 coord = mirrorAlongLines(vec2(texCoord.x-0.5, texCoord.y-0.5));
    vec4 col = texture(InSampler, vec2(coord.x+0.5, coord.y+0.5));
    fragColor = col;
}
