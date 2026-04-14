#version 460

/*
MIT License

Copyright (c) 2024 fayer3

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

#define MC_CLOUD_VERSION 11800

#if MC_CLOUD_VERSION == 11700
  const float VANILLA_CLOUD_HEIGHT = 128.0;
#else
  const float VANILLA_CLOUD_HEIGHT = 192.0;
#endif

const float CLOUD_HEIGHT = 108.00000000;

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 IViewRotMat;

// weird indices, beccause the uniform is actually a mat3 in a mat4, bucause minecraft 1.18 doesn't like mat3 uniforms
mat3 actualIViewRotMat = mat3(
  IViewRotMat[0][0], IViewRotMat[0][1], IViewRotMat[0][2],
  IViewRotMat[0][3], IViewRotMat[1][0], IViewRotMat[1][1],
  IViewRotMat[1][2], IViewRotMat[1][3], IViewRotMat[2][0]);

const mat3 inverseCloudsScale = mat3(
    0.0833, 0.0, 0.0,
    0.0, 1.0, 0.0,
    0.0, 0.0, 0.0833);

uniform mat4 ProjMat;
uniform sampler2D Sampler0;

out vec2 texCoord0;
out vec3 vertexPosition;
out vec4 vertexColor;

void main() {
    texCoord0 = UV0;
    vertexColor = Color;
    
    if (actualIViewRotMat[0].x > 0.999 && actualIViewRotMat[1].y > 0.999 && actualIViewRotMat[2].z > 0.999) {
      // 1.20.5+ removes IViewRotMat, and has the cloud scale in the modelview
      actualIViewRotMat = inverseCloudsScale * transpose(mat3(ModelViewMat));
    }
    
    vec3 newPosition = vec3(Position.x, Position.y+(CLOUD_HEIGHT-VANILLA_CLOUD_HEIGHT), Position.z);
    if (abs(Normal.y) > 0.9) {
      if (Position.x < 7.9  || (Position.x < 8.1 && (gl_VertexID % 4 == 1 || gl_VertexID % 4 == 2))) {
        // usual top/bottom
        texCoord0 = vec2(UV0.x + (((newPosition.x+24.0))/float(textureSize(Sampler0, 0).x)),UV0.y);
        newPosition = vec3((newPosition.x+8.0)*2.0+8.0, newPosition.y, newPosition.z);
        vertexPosition = actualIViewRotMat*(ModelViewMat * vec4(newPosition, 1.0)).xyz;
        gl_Position = ProjMat * ModelViewMat * vec4(newPosition, 1.0);
      }
      else if (abs(Position.y) > 7.5){ // try not to offset if both sides are drawn
        // opposite top/bottom
        texCoord0 = vec2(UV0.x + (((newPosition.x-40.0))/float(textureSize(Sampler0, 0).x)),UV0.y);
        newPosition = vec3((newPosition.x-24.0)*2.0+8.0, newPosition.y-sign(Normal.y)*4.0, newPosition.z);
        vertexColor.rgb *= Normal.y < 0.0 ? vec3(1.4326) : vec3(0.698);
        vertexPosition = actualIViewRotMat*(ModelViewMat * vec4(newPosition, 1.0)).xyz;
        gl_Position = ProjMat * ModelViewMat * vec4(newPosition, 1.0);
      }
      else {
        // these shouldn't be rendered move them outside the frustum
        gl_Position = vec4(-10.0);
      }
    } else {
      vertexPosition = actualIViewRotMat*(ModelViewMat * vec4(newPosition, 1.0)).xyz;
      gl_Position = ProjMat * ModelViewMat * vec4(newPosition, 1.0);
    }
}
