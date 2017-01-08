package zju.homework.augmentedstudio.Shaders;

/**
 * Created by stardust on 2017/1/2.
 */

public class ObjectShader {

    public static final String CUBE_MESH_VERTEX_SHADER = " \n" + "\n"+
            "precision mediump float;\n" +
            "uniform mat4 u_MVPMatrix;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "uniform vec4 u_Color;\n" +
            "\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec3 a_Normal;\n" +
            "attribute vec2 a_TexCoordinate;\n" +
            "\n" +
            "varying vec3 v_Position;\n" +
            "varying vec4 v_Color;\n" +
            "varying vec3 v_Normal;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "     v_Position = vec3(u_MVMatrix * a_Position);\n" +
            "     v_Color = u_Color;\n" +
            "     v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "\n" +
            "     gl_Position = u_MVPMatrix * a_Position;\n" +
            "     v_TexCoordinate = a_TexCoordinate;\n" +
            "}";

    public static final String CUBE_MESH_FRAGMENT_SHADER = " \n" + "\n"+
            "precision mediump float;\n" +
            "uniform vec3 u_LightPos;\n" +
            "uniform sampler2D u_Texture;\n" +
            "\n" +
            "varying vec3 v_Position;\n" +
            "varying vec4 v_Color;\n" +
            "varying vec3 v_Normal;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "    float distance = length(u_LightPos - v_Position);\n" +
            "    vec3 lightVector = normalize(u_LightPos - v_Position);\n" +
            "    float diffuse = max(dot(v_Normal, lightVector), 0.2);\n" +
            "\n" +
            "    diffuse = diffuse * (1.0/ (1.0 + (0.10 * distance)));\n" +
            "    diffuse = diffuse + 0.3;\n" +
            "\n" +
//            "    gl_FragColor = (v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate));\n" +
//            "    gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));\n" +
            "    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);\n" +
            "}";
}
