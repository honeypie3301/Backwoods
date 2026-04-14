#version 460

uniform float AmbientLightFactor;
uniform float SkyFactor;
uniform float BlockFactor;
uniform int UseBrightLightmap;
uniform vec3 SkyLightColor;
uniform float NightVisionFactor;
uniform float DarknessScale;
uniform float DarkenWorldFactor;
uniform float BrightnessFactor;

const float[] BETA_LIGHT = float[](
    0.0434389140271494, 0.0627450980392157, 0.0823529411764706, 0.1019607843137255, 0.1254901960784314, 0.1529411764705882, 
    0.1843137254901961, 0.2196078431372549, 0.2588235294117647, 0.3058823529411765, 0.3647058823529412, 
    0.4352941176470588, 0.5215686274509804, 0.6352941176470588, 0.7843331391962726, 1.0
);

in vec2 texCoord;

out vec4 fragColor;

int spread(float f, int x) {
    return clamp(int(floor(f * (float(x) + 1.0))), 0, x);
}

void main() {
    if (NightVisionFactor > 0.0) {
        fragColor = vec4(vec3(1.0), 1.0);
        return;
    }

    int block_light = spread(texCoord.x, 15);
    int sky_factor = clamp(spread(1.0 - SkyFactor, 15), 0, 11);
    int sky_light = clamp(spread(texCoord.y, 15), 0, 15);

    float light = max(BETA_LIGHT[block_light], BETA_LIGHT[sky_light - sky_factor]);
    fragColor = vec4(vec3(clamp(light - DarknessScale * 0.7, 0.05, 1)), 1.0);
}
