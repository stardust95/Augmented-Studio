package zju.homework.augmentedstudio.Models;

import android.content.res.AssetManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by stardust on 2016/12/26.
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelObject extends MeshObject implements Serializable {

    private ByteBuffer verts;
    private ByteBuffer textCoords;
    private ByteBuffer norms;

    int numVerts = 0;


    public void loadTextModel(AssetManager assetManager, String filename)
            throws IOException
    {
        InputStream is = null;

        is = assetManager.open(filename);
        modelName = filename;
//        modelName = filename.lastIndexOf('\\') >= 0 ? filename.substring(filename.lastIndexOf('\\')) : filename;

        loadTextModel(is);
    }

    public void loadTextModel(String filename) throws IOException{
        modelName = filename;
        loadTextModel(new FileInputStream(filename));
    }

    public void loadTextModel(InputStream is) throws IOException{
        try{
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));

            String line = reader.readLine();

            int floatsToRead = Integer.parseInt(line);
            numVerts = floatsToRead / 3;

            verts = ByteBuffer.allocateDirect(floatsToRead * 4);
            verts.order(ByteOrder.nativeOrder());
            for (int i = 0; i < floatsToRead; i++)
            {
                verts.putFloat(Float.parseFloat(reader.readLine()));
            }
            verts.rewind();

            line = reader.readLine();
            floatsToRead = Integer.parseInt(line);

            norms = ByteBuffer.allocateDirect(floatsToRead * 4);
            norms.order(ByteOrder.nativeOrder());
            for (int i = 0; i < floatsToRead; i++)
            {
                norms.putFloat(Float.parseFloat(reader.readLine()));
            }
            norms.rewind();

            line = reader.readLine();
            floatsToRead = Integer.parseInt(line);

            textCoords = ByteBuffer.allocateDirect(floatsToRead * 4);
            textCoords.order(ByteOrder.nativeOrder());
            for (int i = 0; i < floatsToRead; i++)
            {
                textCoords.putFloat(Float.parseFloat(reader.readLine()));
            }
            textCoords.rewind();

        }finally{
            if (is != null)
                is.close();
        }
    }


    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                if( verts.order() != ByteOrder.nativeOrder() )
                    verts = verts.order(ByteOrder.nativeOrder());
                result = verts;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                if( textCoords.order() != ByteOrder.nativeOrder() )
                    textCoords = textCoords.order(ByteOrder.nativeOrder());
                result = textCoords;
                break;
            case BUFFER_TYPE_NORMALS:
                if( norms.order() != ByteOrder.nativeOrder() )
                    norms = norms.order(ByteOrder.nativeOrder());
                result = norms;
            default:
                break;
        }

        return result;
    }

    @JsonIgnore
    @Override
    public int getNumObjectVertex()
    {
        return numVerts;
    }

    @JsonIgnore
    @Override
    public int getNumObjectIndex()
    {
        return 0;
    }

    public ByteBuffer getVerts() {
        if( verts.order() != ByteOrder.nativeOrder() )
            verts = verts.order(ByteOrder.nativeOrder());
        return verts;
    }

    public void setVerts(ByteBuffer verts) {
        this.verts = verts;
    }

    public ByteBuffer getTextCoords() {
        if( textCoords.order() != ByteOrder.nativeOrder() )
            textCoords = textCoords.order(ByteOrder.nativeOrder());
        return textCoords;
    }

    public void setTextCoords(ByteBuffer textCoords) {
        this.textCoords = textCoords;
    }

    public ByteBuffer getNorms() {
        if( norms.order() != ByteOrder.nativeOrder() )
            norms = norms.order(ByteOrder.nativeOrder());
        return norms;
    }

    public void setNorms(ByteBuffer norms) {
        this.norms = norms;
    }

    public int getNumVerts() {
        return numVerts;
    }

    public void setNumVerts(int numVerts) {
        this.numVerts = numVerts;
    }
}
