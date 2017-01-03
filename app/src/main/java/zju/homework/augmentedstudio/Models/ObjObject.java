package zju.homework.augmentedstudio.Models;

import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

//import zju.homework.augmentedstudio.Utils.GeomBuilder;
//import zju.homework.augmentedstudio.Utils.ObjReader;
import zju.homework.augmentedstudio.Utils.Tools.Vertex;

/**
 * Created by stardust on 2017/1/2.
 */
public class ObjObject extends MeshObject{

    public static final String TAG = "Mesh";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer normalsBuffer;
    private FloatBuffer texCoordsBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mNormalsHandle;

    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private final int mTextureCoordinateDataSize = 2;
    private int mTextureDataHandle;

    private float[] lightPos = {1, 2, 2};

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private final List<Short> indices;
    private final List<Vertex> vertices;

    public final int texVertexStride = mTextureCoordinateDataSize * 4;
    public final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    //float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
    float color[] = { 1.0f, 0.709803922f, 0.898039216f, 1.0f };


    // Constructor
    public ObjObject(List<Short> indices, List<Vertex> vertices) {


        this.vertices = vertices;
        this.indices = indices;

    }

//    public void setLighting(float[] mvMatrix) {
//
//        int mvMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
////        MyGLRenderer.checkGlError("get model view matrix");
//        int lightLocHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
////        MyGLRenderer.checkGlError("get light location");
//
//        GLES20.glUniformMatrix4fv(mvMatrixHandle, 1, false, mvMatrix, 0);
//        MyGLRenderer.checkGlError("set model view matrix");
//
//        GLES20.glUniform3fv(lightLocHandle, 1, lightPos, 0);
//        MyGLRenderer.checkGlError("light location set");
//    }

    public void initialize() {

        int i = 0;
        int j = 0;
        int k = 0;

        float[] v = new float[vertices.size()*3];
        float[] vn = new float[vertices.size()*3];
        float[] uv = new float[vertices.size()*2];
        short[] f = new short[indices.size()];

        for (Vertex vertex: vertices) {

            v[i++] = vertex.position.x;
            v[i++] = vertex.position.y;
            v[i++] = vertex.position.z;

            uv[j++] = vertex.tex.x;
            uv[j++] = vertex.tex.y;

            vn[k++] = vertex.normal.x;
            vn[k++] = vertex.normal.y;
            vn[k++] = vertex.normal.z;
        }

        int l = 0;
        for (Short face: indices) {
            f[l++] = face;
        }


        Log.v(TAG, "V length:" + v.length);
        Log.v(TAG, "F length:" + f.length);
        Log.v(TAG, "UV length:" + uv.length);
        Log.v(TAG, "VN length:" + vn.length);
        Log.v(TAG, "Vertices size:" + vertices.size());

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                v.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(v);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                f.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(f);
        drawListBuffer.position(0);

        // initialize normals
        ByteBuffer nlb = ByteBuffer.allocateDirect(
                vn.length * 4);
        nlb.order(ByteOrder.nativeOrder());
        normalsBuffer = nlb.asFloatBuffer();
        normalsBuffer.put(vn);
        normalsBuffer.position(0);

        // set up UV texCoords
        ByteBuffer uvlb = ByteBuffer.allocateDirect(
                uv.length * 4);
        uvlb.order(ByteOrder.nativeOrder());
        texCoordsBuffer = uvlb.asFloatBuffer();
        texCoordsBuffer.put(uv);
        texCoordsBuffer.position(0);


//        String vertexSource = ResourceLoader.getResourceLoader().readTextFile("shaders/basic.vs");
//        String fragSource = ResourceLoader.getResourceLoader().readTextFile("shaders/basic.fs");

//        if (vertexSource == null || fragSource == null) {
//            Log.e(TAG, "Could not load the files");
//            return;
//        }
//
//        // prepare shaders and OpenGL program
//        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
//                vertexSource);
//        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
//                fragSource);
//
//        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
//        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
//        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
//        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
//        MyGLRenderer.checkGlError("linking the shader");
//
//        GLES20.glValidateProgram(mProgram);
//        Log.v(TAG, "Error Log:" + GLES20.glGetProgramInfoLog(mProgram));
//
//        ResourceLoader loader = ResourceLoader.getResourceLoader();
//
//        mTextureDataHandle = loader.loadTexture("textures/alduin.etc");
//
//        Log.v(TAG, "Loading shader successful");
    }
//
//    public void draw(float[] mvpMatrix, float[] mvMatrix) {
////
////        // Add program to OpenGL environment
////        GLES20.glUseProgram(mProgram);
////        MyGLRenderer.checkGlError("Use Program");
////
////        // get handle to vertex shader's vPosition member
////        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
////        MyGLRenderer.checkGlError("get Position attribute");
////        mNormalsHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");
////        MyGLRenderer.checkGlError("get Normals attribute");
////
////        mColorHandle = GLES20.glGetUniformLocation(mProgram, "u_Color");
////        MyGLRenderer.checkGlError("get Color uniform");
//        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
//        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
//
//        // Enable a handle to vertices
//        GLES20.glEnableVertexAttribArray(mPositionHandle);
//        GLES20.glEnableVertexAttribArray(mNormalsHandle);
//        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
//
//        // Prepare the coordinate data
//        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
//                GLES20.GL_FLOAT, false,
//                vertexStride, vertexBuffer);
//
//        GLES20.glVertexAttribPointer(mNormalsHandle, COORDS_PER_VERTEX,
//                GLES20.GL_FLOAT, false,
//                vertexStride, normalsBuffer);
////        MyGLRenderer.checkGlError("set normals attribute pointer");
//
//        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize,
//                GLES20.GL_FLOAT, false,
//                texVertexStride, texCoordsBuffer);
////        MyGLRenderer.checkGlError("set texture coordinate attribute pointer");
//
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
//
//
//        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
////        MyGLRenderer.checkGlError("get MVP Matrix Uniform");
//
//        // Apply the projection and view transformation
//        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
////        MyGLRenderer.checkGlError("set MVP Matrix Uniform");
//
////        setLighting(mvMatrix);
//
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
//        GLES20.glUniform1i(mTextureUniformHandle, 0);
//
//        // Draw the square
//        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size(),
//                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
//
//        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mNormalsHandle);
//        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
//    }

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType) {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = vertexBuffer;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = texCoordsBuffer;
                break;
            case BUFFER_TYPE_NORMALS:
                result = normalsBuffer;
            case BUFFER_TYPE_INDICES:
                result = drawListBuffer;
            default:
                break;
        }

        return result;
    }

    @Override
    public int getNumObjectVertex() {
        return 0;
    }

    @Override
    public int getNumObjectIndex() {
        return indices.size();
    }

}