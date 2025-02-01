#version 150

uniform sampler2D InSampler;
uniform sampler2D RankSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int ListSize;
uniform vec2 Direction;

const int MAX_SIZE = 256;

float directionAngle = atan(Direction.y, Direction.x);
vec2 texOffset = texCoord*vec2(sin(directionAngle), cos(directionAngle));

vec2 GetListCoord(float offset) {
    return abs(vec2(offset)*oneTexel*Direction + texOffset);
}

int GetNthMinIndex(float[MAX_SIZE] ranks, int entries, int n) {
    float minRank;
    int minIndex;
    float rank;

    for (int i = 0; i <= n; i++) {
        minRank = 2.0;
        minIndex = 0;

        for (int j = 0; j < entries; j++) {
            rank = ranks[j];
            if (rank < minRank) {
                minRank = rank;
                minIndex = j;
            }
        }

        ranks[minIndex] = 2.0;
    }

    return minIndex;
}

void main(){
    int listSize = min(ListSize, MAX_SIZE);

    int pixelCoord = int((texCoord.x/oneTexel.x + 0.5)*Direction.x) + int((texCoord.y/oneTexel.y + 0.5)*Direction.y);
    int listIndex = pixelCoord%listSize;
    int listStart = (pixelCoord/listSize)*listSize;

    float ranks[MAX_SIZE];
    for (int i = 0; i < listSize; i++) {
        ranks[i] = texture(RankSampler, GetListCoord(listStart+i)).r;
    }

    int index = GetNthMinIndex(ranks, listSize, listIndex);
    vec2 coord = GetListCoord(listStart+index);
    vec3 color = texture(InSampler, coord).rgb;

    fragColor = vec4(color, 1.0);
}
