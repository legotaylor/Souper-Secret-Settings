#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform vec3 Color1;
uniform vec3 Color2;
uniform vec3 Color3;
uniform vec3 Color4;
uniform float Seed;
uniform float luminance_alpha_smooth;

float scalarTripleProduct(vec3 a, vec3 b, vec3 c) {
    return dot(a,cross(b,c));
}

float hash(vec3 p3){
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

float squareDist(vec3 a, vec3 b) {
    vec3 v = a - b;
    return dot(v, v);
}

vec4 getBarycentric(vec3 a, vec3 b, vec3 c, vec3 d, vec3 p) {
    vec3 vap = p - a;
    vec3 vbp = p - b;
    vec3 vab = b - a;
    vec3 vac = c - a;
    vec3 vad = d - a;
    vec3 vbc = c - b;
    vec3 vbd = d - b;

    return vec4(
        scalarTripleProduct(vbp, vbd, vbc),
        scalarTripleProduct(vap, vac, vad),
        scalarTripleProduct(vap, vad, vab),
        scalarTripleProduct(vap, vab, vac)
    );
}

// interpretation of a palette dither method described by https://bsky.app/profile/krisp.bsky.social
// its less cool though as the medium of soup doesnt really allow for precomputing lookup tables, so everything needs to be calculated per fragment

// it also only approximates the tetrahedralisation of the points when the color isnt within the 4 specified
void main() {
    vec3 col = texture(InSampler, texCoord).rgb;

    vec4 barycentric = getBarycentric(Color1, Color2, Color3, Color4, col);

    int count = 0;
    int indices[4] = int[](0,1,2,3);
    for (int i = 0; i < 4; i++) {
        if (barycentric[i] < 0) {
            indices[count] = i;
            count++;
        } else {
            indices[3-count] = i;
        }
    }

    vec3 colors[4] = vec3[](
        Color1, Color2, Color3, Color4
    );

    if (count > 0) {
        vec3 project = col - vec3(colors[indices[count == 1 ? 0 : 3]]);
        colors[indices[0]] = step(0.5, project / max(max(abs(project.x), abs(project.y)), abs(project.z)));

        if (count == 2) {
            vec3 projectB = col - vec3(colors[indices[2]]);
            colors[indices[1]] = step(0.5, projectB / max(max(abs(projectB.x), abs(projectB.y)), abs(projectB.z)));
        } else if (count == 3) {
            vec3 ref = vec3(colors[indices[0]]);

            vec3 comp =  vec3(1-ref.x, ref.y, ref.z);
            float minDistance = squareDist(col, comp);
            float secondDistance = 10000;
            colors[indices[1]] = comp;

            for (int i = 2; i < 8; i++) {
                vec3 comp = vec3(((i&1) > 0) ? 1-ref.x : ref.x, ((i&2) > 0) ? 1-ref.y : ref.y, ((i&4) > 0) ? 1-ref.z : ref.z);
                float d = squareDist(col, comp);
                if (d < minDistance) {
                    secondDistance = minDistance;
                    colors[indices[2]] = colors[indices[1]];
                    minDistance = d;
                    colors[indices[1]] = comp;
                } else if (d < secondDistance) {
                    secondDistance = d;
                    colors[indices[2]] = comp;
                }
            }
        }

        barycentric = getBarycentric(colors[0], colors[1], colors[2], colors[3], col);
    }

    barycentric = max(barycentric, 0);
    vec4 weights = barycentric / (barycentric.x + barycentric.y + barycentric.z + barycentric.w);

    vec2 t = texCoord + vec2(1.234 * Seed, 5.678);
    float random = hash(vec3(t * t, Seed * t.y));

    vec3 result;
    if (random < weights.x) {
        result = colors[0];
    }
    else if (random < weights.x + weights.y) {
        result = colors[1];
    }
    else if (random < weights.x + weights.y + weights.z) {
        result = colors[2];
    }
    else {
        result = colors[3];
    }

    fragColor = vec4(mix(col, result, luminance_alpha_smooth), 1.0);
}
