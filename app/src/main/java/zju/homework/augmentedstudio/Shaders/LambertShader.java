package zju.homework.augmentedstudio.Shaders;

/**
 * Created by stardust on 2017/1/9.
 */

public class LambertShader implements IShader{


    @Override
    public String FRAGMENT_SHADER() {
        return OBJECT_LAMBERT_FRAGMENT_SHADER;
    }

    @Override
    public String VERTEX_SHADER() {
        return OBJECT_LAMBERT_VERTEX_SHADER;
    }

    public static final String OBJECT_LAMBERT_VERTEX_SHADER = " \n" + "\n"+
            "precision mediump float;\n" +
            "uniform mat4 u_MVPMatrix;\n" +
            "uniform mat4 u_MVMatrix;\n" +
            "uniform vec4 u_Color;\n" +
            "\n" +
            "attribute vec4 a_Position;\n" +
            "attribute vec3 a_Normal;\n" +
            "attribute vec2 a_TexCoordinate;\n" +
            "\n" +
            "varying vec4 v_Position;\n" +
            "varying vec4 v_Color;\n" +
            "varying vec4 v_Normal;\n" +
            "varying vec2 v_TexCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "\n" +
            "     v_Position = vec4(u_MVMatrix * a_Position);\n" +
            "     v_Color = u_Color;\n" +
            "     v_Normal = vec4(u_MVMatrix * vec4(a_Normal, 0.0));\n" +
            "\n" +
            "     gl_Position = u_MVPMatrix * a_Position;\n" +
            "     v_TexCoordinate = a_TexCoordinate;\n" +
            "}";


    public static final String OBJECT_LAMBERT_FRAGMENT_SHADER =
           "precision mediump float;\n" +
                   "\n" +
                   "varying vec4 v_Position; // Position in world space.\n" +
                   "varying vec4 v_Normal; // Surface normal in world space.\n" +
                   "varying vec2 v_TexCoordinate;\n" +
                   "varying vec4 v_Color; // Light's diffuse and specular contribution.\n" +
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
                   "\n" +
                   "void main()\n" +
                   "{\n" +
                   "    // Compute the emissive term.\n" +
                   "    vec4 Emissive = MaterialEmissive;\n" +
                   "\n" +
                   "    // Compute the diffuse term.\n" +
                   "    vec4 N = normalize( v_Normal );\n" +
                   "    vec4 u_LightPosW = vec4(u_LightPos, 1.0);\n" +
                   "    vec4 L = normalize( u_LightPosW - v_Position );\n" +
                   "    float NdotL = max( dot( N, L ), 0.0 );\n" +
                   "    vec4 Diffuse =  NdotL * v_Color * u_Diffuse;\n" +
                   "    \n" +
                   "    // Compute the specular term.\n" +
                   "    vec4 u_EyePosW = vec4(u_EyePos, 1.0);\n" +
                   "    vec4 V = normalize( u_EyePosW - v_Position );\n" +
                   "    vec4 H = normalize( L + V );\n" +
                   "    vec4 R = reflect( -L, N );\n" +
                   "    float RdotV = max( dot( R, V ), 0.0 );\n" +
                   "    float NdotH = max( dot( N, H ), 0.0 );\n" +
                   "    vec4 Specular = pow( RdotV, u_Shiness ) * v_Color * u_Specular;\n" +
                   "    if( u_isColorPicking > 1.0 ){\n" +
                   "        gl_FragColor = v_Color;\n" +
                   "    }else{\n" +
                   "        gl_FragColor = ( u_Ambient + Diffuse + Specular ) * texture2D( u_Texture, v_TexCoordinate );\n" +
                   "    }\n" +
                   "}";


}
