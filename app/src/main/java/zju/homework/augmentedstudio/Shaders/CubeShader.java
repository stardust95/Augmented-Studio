package zju.homework.augmentedstudio.Shaders;

/**
 * Created by stardust on 2016/12/24.
 */


public class CubeShader
{

    public static final String CUBE_MESH_VERTEX_SHADER = " \n" + "\n"
            + "attribute vec4 vertexPosition; \n"
            + "attribute vec2 vertexTexCoord; \n" + "\n"
            + "varying vec2 texCoord; \n" + "\n"
            + "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
            + "void main() \n" + "{ \n"
            + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
            + "   texCoord = vertexTexCoord; \n"
            + "} \n";

    public static final String CUBE_MESH_FRAGMENT_SHADER = " \n" + "\n"
            + "precision mediump float; \n" + " \n"
            + "varying vec2 texCoord; \n"
            + "uniform sampler2D texSampler2D; \n" + " \n"
            + "void main() \n"
            + "{ \n"
            + "   gl_FragColor = texture2D(texSampler2D, texCoord); \n"
//            + "   gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); \n"
            + "} \n";

}
