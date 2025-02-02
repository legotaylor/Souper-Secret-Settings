#version 150

uniform sampler2D InSampler;
uniform sampler2D RankSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int SegmentSize;
uniform int MaxLength;
uniform vec4 Scroll;
uniform vec2 Direction;
uniform vec2 LossRate;
uniform int ShowRank;

const int MAX_SIZE = 256;

float directionAngle = atan(Direction.y, Direction.x);
vec2 texOffset = texCoord*vec2(sin(directionAngle), cos(directionAngle));

vec2 GetListCoord(float offset) {
    return abs(vec2(offset)*oneTexel*Direction + texOffset);
}

int GetNthMinIndex(float[MAX_SIZE] ranks, int n, int start, int end) {
    float minRank;
    int minIndex;
    float rank;

    float firstRank = ranks[start];
    for (int i = 0; i <= n; i++) {
        minRank = ranks[start];
        minIndex = start;

        for (int j = start+1; j <= end; j++) {
            rank = ranks[j];
            if (rank < minRank) {
                minRank = rank;
                minIndex = j;
            }
        }

        ranks[minIndex] = 255;
        if (minIndex == start) {
            start++;
        }
        if (minIndex == end) {
            end--;
        }
    }

    return minIndex;
}

float hash(vec3 p3) {
    p3 = fract(p3 * 0.1031);
    p3 += dot(p3,p3.yzx + 19.19);
    return fract((p3.x + p3.y) * p3.z);
}

vec2 hash1D(float v) {
    float p;
    float p2;
    p = hash(vec3(1/v, fract(v*13.2169), pow(2, fract(v)+1.23)));
    p2 = hash(vec3(p*v, p*123.456, p+1.23));
    p = hash(vec3(mod(p2,0.123), p2-15.32 + v, p2*p*8));
    return vec2(p,p2);
}

void main(){
    int listSize = min(SegmentSize, MAX_SIZE);

    vec2 rand = hash1D(length(texOffset)+Scroll.z);
    listSize += int(mod(rand.x*Scroll.y,Scroll.y));
    int scroll = int(rand.y*Scroll.x*listSize + Scroll.w);

    int pixelCoord = int((texCoord.x/oneTexel.x + 0.5)*Direction.x) + int((texCoord.y/oneTexel.y + 0.5)*Direction.y) + scroll;
    int listIndex = pixelCoord%listSize;
    int listStart = (pixelCoord/listSize)*listSize;
    listStart -= scroll;

    float ranks[MAX_SIZE];
    int startIndex = -1;
    int endIndex = listSize-1;
    for (int i = 0; i < listSize; i++) {
        vec2 coord = GetListCoord(listStart+i);
        vec4 rankCol = texture(RankSampler, coord);
        if (startIndex == -1) {
            if (rankCol.b > 0.5) {
                startIndex = i;
            }
        } else {
            if (rankCol.b < 0.5) {
                endIndex = i-1;
                break;
            }
        }

        float value = rankCol.r + (rankCol.g*255.0)-127.0;
        if (hash(vec3(coord.xy, value)) >= LossRate.x) {
            ranks[i] = value;
        } else {
            //the default value in the list is random gpu data
            value = fract(ranks[i]);
            ranks[i] = ((value-0.5) * LossRate.y) + 0.5;
        }
    }

    int runLength = endIndex-startIndex;
    if (runLength > MaxLength) {
        endIndex = startIndex+MaxLength;
    }

    int index;
    if (startIndex == -1 || listIndex < startIndex || listIndex > endIndex) {
        index = listIndex;
    } else {
        index = GetNthMinIndex(ranks, listIndex-startIndex, startIndex, endIndex);
    }

    vec2 coord = GetListCoord(listStart+index);
    vec3 color = texture(ShowRank > 0 ? RankSampler : InSampler, coord).rgb;

    fragColor = vec4(color, 1.0);
}
