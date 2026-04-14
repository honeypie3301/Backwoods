#version 460

#moj_import <fog.glsl>
#moj_import <dynamictransforms.glsl>
#moj_import <projection.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;
uniform sampler2D Sampler2;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 lightMapColor; // Added missing output

void main() {
    vec3 pos = Position + ModelOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);

    // Properly convert UV2 to float and divide
    vec2 uv2f = vec2(UV2) / 16.0;
    lightMapColor = texelFetch(Sampler2, ivec2(uv2f), 0);
    // Lightmap changes depending on the dimension
    vec4 targetColor = vec4(229.0 / 255.0, 229.0 / 255.0, 229.0 / 255.0, 1.0);
    float tolerance = 0.001;
    if (all(lessThan(abs(Color - targetColor), vec4(tolerance)))) { // if nether
        vertexColor = Color * lightMapColor;
        vertexColor *= (252.0 / 229.0);
    } else {  // if overworld or end
        vertexColor = Color * lightMapColor;
    }
    texCoord0 = UV0;
}
