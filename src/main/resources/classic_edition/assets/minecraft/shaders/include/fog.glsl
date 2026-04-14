#version 460

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    // Apply adjustments to fogStart and fogEnd
    fogEnd *= 30.5 / 30.0; // Adjust for better visual match to original beta fog
    fogStart /= 4.0; // Adjust for better visual match to original beta fog
    if (vertexDistance <= fogStart) {
        return inColor;
    }
    float fogValue = vertexDistance < fogEnd ? (vertexDistance - fogStart) / (fogEnd - fogStart) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 1.0;
    } else if (vertexDistance >= fogEnd) {
        return 0.0;
    }
    return smoothstep(fogEnd, fogStart, vertexDistance);
}

float fog_distance(vec3 pos, int shape) {
        return length(pos);
}
