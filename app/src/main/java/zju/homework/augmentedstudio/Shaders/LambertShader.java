package zju.homework.augmentedstudio.Shaders;

import zju.homework.augmentedstudio.Utils.Util;

/**
 * Created by stardust on 2017/1/10.
 */

public class LambertShader implements IShader{

    @Override
    public String VERTEX_SHADER() {
        return VERTEX_SHADER;
    }

    @Override
    public String FRAGMENT_SHADER() {
        return FRAGMENT_SHADER;
    }

    public final static String VERTEX_SHADER = "\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "uniform mat4 u_MVPMatrix;\n" +
            "uniform mat4 u_ViewMatrix;\n" +
            "// lights\n" +
            "uniform vec3 u_LightPos;\n" +
            "\n" +
            "// vertex attributes\n" +
            "attribute vec3 a_Position;\n" +
            "attribute vec3 a_Normal;\n" +
            "attribute vec2 a_TexCoordinate;\n" +
            "\n" +
            "// pass the vertex and color information to the fragment shader\n" +
            "varying vec3 v_normal;\n" +
            "varying vec2 v_textureCoords;\n" +
            "varying vec3 lightDir;\n" +
            "varying vec3 viewDir;\n" +
            "\n" +
            "// Shader entry point\n" +
            "void main()\n" +
            "{\n" +
            "    // calculate the final position of the vertex\n" +
            "    mat4 mvMatrix = u_MVMatrix;\n" +
            "    mat4 mvpMatrix = u_MVPMatrix;\n" +
            "    \n" +
            "    gl_Position = mvpMatrix * vec4(a_Position, 1.0);\n" +
            "    \n" +
            "    // compute vertex and normal coordinates in ModelView space\n" +
            "    vec3 v_position = vec3(mvMatrix * vec4(a_Position, 1.0));\n" +
            "    v_normal = normalize(vec3(mvMatrix * vec4(a_Normal, 0.0)));\n" +
            "    v_textureCoords = a_TexCoordinate;\n" +
            "    \n" +
            "    vec3 mvLightPos = vec3(u_ViewMatrix * vec4(u_LightPos, 0.0));\n" +
            "    lightDir = normalize(mvLightPos.xyz - v_position.xyz);\n" +
            "    viewDir = normalize(-v_position);\n" +
            "}";

    public final static String FRAGMENT_SHADER =
            "precision mediump float; // use medium precision\n" +
                    "\n" +
                    "// texture\n" +
                    "uniform sampler2D u_Texture;\n" +
                    "uniform int u_TextureEnable;\n" +
                    "uniform int u_isColorPicking;\n" +
                    "\n" +
                    "// material properties\n" +
                    "uniform vec3 u_Ambient;\n" +
                    "uniform vec3 u_Diffuse;\n" +
                    "uniform vec3 u_Specular;\n" +
                    "uniform float u_Alpha;\n" +
                    "uniform float u_Shines;\n" +
                    "uniform vec4 u_Color;\n" +
                    "\n" +
                    "// receive the interpolated values from the vertex shader\n" +
                    "varying vec3 v_normal;\n" +
                    "varying vec2 v_textureCoords;\n" +
                    "\n" +
                    "varying vec3 lightDir;\n" +
                    "varying vec3 viewDir;\n" +
                    "\n" +
                    "// Shader entry point\n" +
                    "void main()\n" +
                    "{\n" +
                    "    float lambertian = max(dot(v_normal, lightDir), 0.1);\n" +
                    "    float specular = 0.0;\n" +
                    "    \n" +
                    "    if (lambertian > 0.0) {\n" +
                    "        vec3 reflectDir = reflect(lightDir, v_normal);\n" +
                    "        float specAngle = max(dot(reflectDir, viewDir), 0.0);\n" +
                    "        specular = pow(specAngle, u_Shines);\n" +
                    "    }\n" +
                    "    \n" +
                    "    vec3 texColor = vec3(1.0, 1.0, 1.0);\n" +
                    "    float alpha = u_Alpha;\n" +
                    "    if (u_TextureEnable == 1) {\n" +
                    "        vec4 texColor4 = texture2D(u_Texture, v_textureCoords);\n" +
                    "        texColor = vec3(texColor4);\n" +
                    "        if (texColor4.a < 1.0)\n" +
                    "            alpha = texColor4.a;\n" +
                    "    }\n" +
                    "    if( u_isColorPicking == 0 ){\n" +
                    "        if( u_TextureEnable == 1 )\n" +
                    "            gl_FragColor =u_Color * 2.0 * vec4(u_Ambient * texColor + u_Diffuse * texColor + specular * u_Specular, alpha);\n" +
                    "        else \n" +
                    "            gl_FragColor = u_Color * vec4(u_Ambient * texColor + lambertian * u_Diffuse * texColor + specular * u_Specular, alpha);\n" +
                    "    } else\n" +
                    "        gl_FragColor = u_Color;\n" +
                    "}";

}
