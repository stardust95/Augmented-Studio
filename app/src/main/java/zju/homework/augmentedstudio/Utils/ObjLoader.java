package zju.homework.augmentedstudio.Utils;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by stardust on 2017/1/2.
 */

public class ObjLoader {

    int len;

    ArrayList<Vector3> VertexStore;
    ArrayList<Vector3> NormalStore;
    ArrayList<Vector3> TextureStore;
    ArrayList<Face> FaceStore;
    int numFaces;

    short[] ind;
    float[] verts;
    float[] norms;

    float[] vtx;
    FloatBuffer vertsBuffer;
    FloatBuffer textsBuffer;
    FloatBuffer normsBuffer;
    ShortBuffer indicesBuffer;

    public ObjLoader()
    {
        VertexStore = new ArrayList<Vector3>();
        NormalStore = new ArrayList<Vector3>();
        TextureStore = new ArrayList<Vector3>();
        FaceStore = new ArrayList<Face>();
    }

    public FloatBuffer getVertsBuffer() {
        return vertsBuffer;
    }

    public FloatBuffer getTextsBuffer() {
        return textsBuffer;
    }

    public FloatBuffer getNormsBuffer() {
        return normsBuffer;
    }

    public ShortBuffer getIndicesBuffer() {
        return indicesBuffer;
    }

    public void loadObj(String filename)
    {
        long start = System.currentTimeMillis();
        StringBuilder model = new StringBuilder("");
        String line;
        try {
            InputStream is = new FileInputStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            line = reader.readLine();
            while(line != null)
            {
                model.append(line);
                model.append("\n");
                line = reader.readLine();
            }
            long end = System.currentTimeMillis();
            System.out.println("total time "+(end-start)+" ms");
            is.close();

        } catch (IOException e) { e.printStackTrace();	}
        parseModel(model.toString());
    }

    public void parseModel(String model)
    {
        String[] lines = model.split("\n");
        len = lines.length;

        for (int i = 0; i < len; i++) {
            String l = lines[i];
            if(l.startsWith("#")){
                continue;
            }

            if(l.startsWith("v ")){
                String[] verts = l.split("[ ]");
                parseVertexStore(i, l, verts);
            }

            if(l.startsWith("vn ")){
                String[] NormalStores = l.split("[ ]");
                parseNormalStores(i, l, NormalStores);
            }

            if(l.startsWith("vt ")){
                @SuppressWarnings("unused")
                String[] texture = l.split("[ ]+");

                //nothing for now
                continue;
            }

            if(l.startsWith("f ")){
                String[] faces = l.split("[ ]+");
                parseFaces(i, l, faces);
            }
        }

        for(int i = 0; i < VertexStore.size(); i++)
        {
            //System.out.println("VertexStore num : "+i+" "+VertexStore.get(i).toString());
        }
        //System.out.println("============================");
        for(int i = 0; i < NormalStore.size(); i++)
        {
            //System.out.println("NormalStore  num : "+i+" "+NormalStore.get(i).toString());
        }
        //System.out.println("============================");
        for(int i = 0; i < numFaces; i++)
        {
            //System.out.println("faces "+faces.get(i).toString());
        }
        //System.out.println("============================");
        ind = new short[numFaces*3];
        int indx = 0;
        for(int i = 0; i < numFaces; i++)
        {
            ind[indx++] = (short) FaceStore.get(i).x;
            ind[indx++] = (short) FaceStore.get(i).y;
            ind[indx++] = (short) FaceStore.get(i).z;
        }
        indx = 0;
        for(int i = 0; i < numFaces*3; i+=3)
        {
            System.out.println("index buffer "+ind[indx++]+" "+ind[indx++]+" "+ind[indx++]);
        }
        indx = 0;
        verts = new float[VertexStore.size()*3];
        for(int i = 0; i < VertexStore.size(); i++)
        {
            verts[indx++] = VertexStore.get(i).x;
            verts[indx++] = VertexStore.get(i).y;
            verts[indx++] = VertexStore.get(i).z;
        }
        indx = 0;
        for(int i = 0; i < VertexStore.size()*3; i+=3)
        {
            //System.out.println("VertexStore buffer "+i+" || "+verts[indx++]+" "+verts[indx++]+" "+verts[indx++]);
        }
        vtx = new float[numFaces*9];
        indx = 0;
        for(int i = 0; i < numFaces; i++)
        {
            vtx[indx++] = VertexStore.get(FaceStore.get(i).x).x;
            vtx[indx++] = VertexStore.get(FaceStore.get(i).x).y;
            vtx[indx++] = VertexStore.get(FaceStore.get(i).x).z;

            vtx[indx++] = VertexStore.get(FaceStore.get(i).y).x;
            vtx[indx++] = VertexStore.get(FaceStore.get(i).y).y;
            vtx[indx++] = VertexStore.get(FaceStore.get(i).y).z;

            vtx[indx++] = VertexStore.get(FaceStore.get(i).z).x;
            vtx[indx++] = VertexStore.get(FaceStore.get(i).z).y;
            vtx[indx++] = VertexStore.get(FaceStore.get(i).z).z;

        }
        indx = 0;
        for(int i = 0; i < vtx.length; i+=3)
        {
            //System.out.println("VertexStore "+vtx[indx++]+" "+vtx[indx++]+" "+vtx[indx++]);
        }
        vertsBuffer = ByteBuffer.allocateDirect(verts.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertsBuffer.put(verts).position(0);

        indicesBuffer = ByteBuffer.allocateDirect(ind.length*2).order(ByteOrder.nativeOrder()).asShortBuffer();
        indicesBuffer.put(ind).position(0);
    }

    private void parseTexCoordStore()

    private void parseVertexStore(int i, String l, String[] vert){
        //System.out.println("================== VertexStore number "+i);
        String[] v = l.split("[ ]");
        VertexStore.add(new Vector3(Float.parseFloat(v[1]), Float.parseFloat(v[2]), Float.parseFloat(v[3])));
        //System.out.println(" x "+(Float.parseFloat(v[1])+" y "+(Float.parseFloat(v[2])+" z "+(Float.parseFloat(v[3])))));
    }
    private void parseNormalStores(int i, String l, String[] norms){
        //System.out.println("================== NormalStore number "+i);
        String[] n = l.split("[ ]");
        NormalStore.add(new Vector3(Float.parseFloat(n[1]), Float.parseFloat(n[1]), Float.parseFloat(n[2]), Float.parseFloat(n[3])));
        //System.out.println(" x "+(Float.parseFloat(n[1])+" y "+(Float.parseFloat(n[2])+" z "+(Float.parseFloat(n[3])))));
    }

    private void parseFaces(int i, String s, String[] index){
        String[] fi = s.split("[ ]");

        String[] a = fi[1].split("[/]");
        String[] b = fi[2].split("[/]");
        String[] c = fi[3].split("[/]");

        FaceStore.add(new Face((Integer.valueOf(a[0])-1), (Integer.valueOf(b[0])-1), (Integer.valueOf(c[0])-1), (Integer.valueOf(a[2])-1)));
        numFaces++;
    }

    class Face
    {
        int x, y, z, n;

        public Face(int ...fs)
        {
            x = fs[0];
            y = fs[1];
            z = fs[2];
            n = fs[3];
        }

        @Override
        public String toString()
        {
            return "( x "+x+" y "+y+" z "+z+" n "+n+" )";
        }
    }

    class Vector3
    {
        float x, y, z;

        public Vector3()
        {
            x = y = z = 0;
        }

        public Vector3(float ...fs)
        {
            x = fs[0];
            y = fs[1];
            z = fs[2];
        }

        @Override
        public String toString()
        {
            return "( "+x+" "+y+" "+z+" )";
        }
    }
}