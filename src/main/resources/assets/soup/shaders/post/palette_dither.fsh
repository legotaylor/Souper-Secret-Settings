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
// its less cool though as the medium of soup doesnt really allow for precomputing lookup tables
// and tetrahedralisation is hard on the gpu, so instead i just fallback to unit cube when the colors are out of bounds
void main() {
    vec3 base = texture(InSampler, texCoord).rgb;
    vec3 col = base*base;

    vec4 barycentric = getBarycentric(Color1, Color2, Color3, Color4, col);

    int count = 0;
    for (int i = 0; i < 4; i++) {
        if (barycentric[i] < 0) {
            count++;
        }
    }

    vec3 colors[4] = vec3[](
        Color1, Color2, Color3, Color4
    );

    // https://cs.stackexchange.com/questions/89910/how-to-decompose-a-unit-cube-into-tetrahedra
    if (count > 0) {
        colors[0] = vec3(0);
        colors[3] = vec3(1);

        if (col.x <= col.z && col.z <= col.y) {
            colors[1] = vec3(0, 1, 0);
            colors[2] = vec3(0, 1, 1);
        }
        else if (col.x <= col.y && col.y <= col.z) {
            colors[1] = vec3(0, 1, 1);
            colors[2] = vec3(0, 0, 1);
        }
        else if (col.y <= col.x && col.x <= col.z) {
            colors[1] = vec3(0, 0, 1);
            colors[2] = vec3(1, 0, 1);
        }
        else if (col.y <= col.z && col.z <= col.x) {
            colors[1] = vec3(1, 0, 1);
            colors[2] = vec3(1, 0, 0);
        }
        else if (col.z <= col.y && col.y <= col.x) {
            colors[1] = vec3(1, 0, 0);
            colors[2] = vec3(1, 1, 0);
        }
        else {
            colors[1] = vec3(1, 1, 0);
            colors[2] = vec3(0, 1, 0);
        }

        barycentric = getBarycentric(colors[0], colors[1], colors[2], colors[3], col);
    }

    barycentric = max(barycentric, 0);
    vec4 weights = barycentric / (barycentric.x + barycentric.y + barycentric.z + barycentric.w);

    vec2 t = texCoord + vec2(1.234 * Seed, 5.678);
    float r = hash(vec3(t * t, Seed * t.y));
    float random = hash(vec3(texCoord + vec2(r), fract(t.x - r)));

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

    fragColor = vec4(mix(base, sqrt(result), luminance_alpha_smooth), 1.0);
}
