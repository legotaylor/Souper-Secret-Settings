#version 150

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform ivec3 Value;

float xor(float c, int v) {
    return (int(c*255.0) ^ v)/255.0;
}

void main(){
    vec4 col = texture(InSampler, texCoord);

    fragColor = vec4(vec3(xor(col.r, Value.r), xor(col.g, Value.g), xor(col.b, Value.b)), 1.0);
}
