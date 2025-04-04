#version 150

uniform sampler2D InSampler;
uniform sampler2D GridSampler;

in vec2 texCoord;

out vec4 fragColor;

uniform ivec2 Grid;
uniform ivec2 Offset;
uniform vec4 Clear;

void main() {
    int index = Grid.x + Offset.x;
    int size = Grid.y + Offset.y;
    int grid = int(ceil(sqrt(size)));

    int x = index%grid;
    int y = (grid-1 - index/grid);

    vec2 coord = texCoord*grid - vec2(x, y);
    if (ceil(float(size)/grid) < grid) {
        coord.y += 0.5;
    }

    vec3 col;
    if (coord.x < 0 || coord.x > 1 || coord.y < 0 || coord.y > 1) {
        col = mix(texture(GridSampler, texCoord).rgb, Clear.rgb, Clear.a);
    } else {
        col = texture(InSampler, coord).rgb;
    }

    fragColor = vec4(col, 1.0);
}
