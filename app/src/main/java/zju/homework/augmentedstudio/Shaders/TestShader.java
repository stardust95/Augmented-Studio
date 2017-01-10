package zju.homework.augmentedstudio.Shaders;

/**
 * Created by stardust on 2017/1/9.
 */

public class TestShader implements IShader {
    @Override
    public String VERTEX_SHADER() {
        return VERTEX_SHADER;
    }

    @Override
    public String FRAGMENT_SHADER() {
        return FRAGMENT_SHADER;
    }

    public static String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "uniform vec4 u_Color;\n" +
            "\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec3 a_Normal;\n" +
            "attribute vec2 a_TexCoordinate;\n" +
            "\n" +
            "uniform vec3 u_LightPos;\n" +
            "uniform vec3 u_EyePos;   // Eye position in world space.\n" +
            "\n" +
            "varying vec4 varColor;\n" +
            "uniform sampler2D u_Texture;\n" +
            "\n" +
            "void main()  {\n" +
            "     vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n" +
            "\n" +
            "     vec3 modelViewNormal = normalize(vec3(u_MVMatrix * vec4(a_Normal, 0.0)));\n" +
            "\n" +
            "     float distance = length(u_LightPos - modelViewVertex);\n" +
            "     vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n" +
            "\n" +
            "     float LambertTerm = max(dot(modelViewNormal, lightVector), 0.1);\n" +
            "     float diffuse = LambertTerm * (1.0 / (1.0 + (0.5 * distance * distance)));\n" +
            "\n" +
            "     vec3 R = reflect(-lightVector, modelViewNormal); \n" +
            "     vec3 vEye = u_EyePos; \n" +
            "     float specular = 1.0 * 1.0 * pow(max(dot(R, vEye), 0.0), 2.0);  \n" +
            "\n" +
//            "     varColor = ((u_Color * 0.2) + u_Color * diffuse * specular) * texture2D( u_Texture, a_TexCoordinate );\n" +
            "     varColor = (u_Color * diffuse * specular) + texture2D( u_Texture, a_TexCoordinate );\n" +
            "     gl_Position = u_MVPMatrix * a_Position; \n" +
            "\n" +
            "\n" +
            "} ";

    public static String FRAGMENT_SHADER =
            "precision mediump float; \n" +
            "varying vec4 varColor;\n" +
            "\n" +
            "uniform vec3 u_EyePos;   // Eye position in world space.\n" +
            "uniform vec3 u_LightPos; // Light's position in world space.\n" +
            "\n" +
            "uniform vec4 MaterialEmissive;\n" +
            "uniform vec4 u_Diffuse;\n" +
            "uniform vec4 u_Specular;\n" +
            "uniform float u_Shiness;\n" +
            "\n" +
            "uniform float u_isColorPicking;\n" +
            "\n" +
            "uniform vec4 u_Ambient; // Global ambient contribution.\n" +
            "\n" +
            "uniform sampler2D u_Texture;\n" +
            "void main() { \n" +
            "  gl_FragColor = varColor; \n" +
            "}  \n";;
}
