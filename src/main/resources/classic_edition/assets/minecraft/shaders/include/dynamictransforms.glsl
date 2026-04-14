#version 460
#ifndef _DYNAMICTRANSFORMS_GLSL
#define _DYNAMICTRANSFORMS_GLSL

layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
    float LineWidth;
};

#endif // _DYNAMICTRANSFORMS_GLSL