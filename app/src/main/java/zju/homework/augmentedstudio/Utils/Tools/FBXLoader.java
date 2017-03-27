//package zju.homework.augmentedstudio.Utils.Tools;
//
///**
// * Created by stardust on 2017/1/11.
// */
//
//
//import android.content.Context;
//import android.util.Log;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//
//import zju.homework.augmentedstudio.Models.ObjObject;
//
//public class FBXLoader
//{
//    /**
//     * Returns a loaded 3D obj model. The context argument pertains to the current
//     * activity that contains the intended model resource to be loaded. The
//     * resource ID must point to a valid resource or else the method will return null.
//     *
//     * @param context		the context from the currently active Activity.
//     * @param resourceID	the resource ID associated with the intended file to load.
//     * @param fileData   the string containing the file location.
//     * @return				an OGLModel file that contains the loaded models information.
//     */
//    public synchronized static ObjObject loadModel(final Context context, final int resourceID, final String fileData)
//    {
//        ArrayList<Float> vertexBuffer = new ArrayList<Float>();
//        ArrayList<Float> normalBuffer = new ArrayList<Float>();
//        ArrayList<Float> textureBuffer = new ArrayList<Float>();
//
//        InputStream inputStream;
//        String inputLine;
//        ObjObject outputModel = null;
//        float[] heightData = new float[2];
//
//        if(fileData.length() == 0)
//            inputStream = context.getResources().openRawResource(resourceID);
//        else
//        {
//            try
//            {
//                String filePath = "";
//                File fileDir = context.getFilesDir();
//
//                if(fileDir != null)
//                    filePath = fileDir.getPath();
//
//                if(fileData.startsWith(filePath))// "/data/data/"))
//                {
//                    String subString = fileData.substring(fileData.lastIndexOf("/") + 1);
//                    inputStream = context.openFileInput(subString);
//                }
//                else
//                    inputStream = new FileInputStream(fileData);
//            }
//            catch(Exception e1)
//            {
//                inputStream = null;
//            }
//        }
//
//        if(inputStream == null)
//            return null;
//
//        InputStreamReader iStreamReader = new InputStreamReader(inputStream);
//        BufferedReader bufferedReader = new BufferedReader(iStreamReader);
//
//        try
//        {
//            // Loop through the file and attempt to read in the model data.
//            while((inputLine = bufferedReader.readLine()) != null)
//            {
//                if(inputLine.contains("texture") || inputLine.startsWith("vt"))
//                {
//                    readUVs(bufferedReader, inputLine, textureBuffer);
//                }
//                else if(inputLine.contains("normals") || inputLine.startsWith("vn"))
//                {
//                    readNormals(bufferedReader, inputLine, normalBuffer);
//                }
//                else if(inputLine.contains("vertices") || inputLine.startsWith("v"))
//                {
//                    heightData = readVertices(bufferedReader, inputLine, vertexBuffer);
//                }
//                else if(inputLine.contains("faces") || inputLine.startsWith("f"))
//                {
//                    outputModel = buildObjBuffer(bufferedReader, inputLine, vertexBuffer, normalBuffer, textureBuffer);
//                }
//            }
//
//            vertexBuffer.clear();
//            normalBuffer.clear();
//            textureBuffer.clear();
//
//            bufferedReader.close();
//            iStreamReader.close();
//            inputStream.close();
//        }
//        catch(Exception e)
//        {
//            if(e.getLocalizedMessage() != null)
//                Log.e("OBJ Load Error", e.getLocalizedMessage());
//            else if(e.getMessage() != null)
//                Log.e("OBJ Load Error", e.getMessage());
//            else
//                Log.e("OBL Load Error", "Exception message was NULL");
//
//            return null;
//        }
//
//        if(outputModel != null)
//        {
//            float[] bounds = new float[] { heightData[2], heightData[3], heightData[4], heightData[5], heightData[6], heightData[7] };
//
//            outputModel.setMidPoint(heightData[0]);
//            outputModel.setHeight(heightData[1]);
//            outputModel.setBoundingBox(new BoundingBox(bounds));
//            outputModel.setName("");
//        }
//
//        return outputModel;
//    }
//
//    /**
//     * A private method that reads in from a buffered reader and attempts to populate an
//     * array list with the available vertices with each 3 array values in a sequence
//     * representing the x, y, and z values for the vertex.
//     *
//     * @param bufferedReader	a buffered reader of the currently opened file.
//     * @param vertexBuffer		the arraylist to populate with the vertex values.
//     */
//    private static float[] readVertices(final BufferedReader bufferedReader, String input, ArrayList<Float> vertexBuffer)
//    {
//        String inputLine = input;
//        String[] splitString;
//        float minVal = 0.0f;
//        float maxVal = 0.0f;
//        float[] xMinMax = new float[] { 0.0f, 0,0f };
//        float[] yMinMax = new float[] { 0.0f, 0,0f };
//        float[] zMinMax = new float[] { 0.0f, 0,0f };
//        float[] output = new float[2];
//
//        try
//        {
//            if(input.contains("vertices"))
//                inputLine = bufferedReader.readLine();
//
//            while(inputLine.startsWith("v"))
//            {
//                splitString = inputLine.split("\\s+");
//
//                if(splitString.length >= 4)
//                {
//                    Float xVal = Float.parseFloat(splitString[1]);
//                    Float yVal = Float.parseFloat(splitString[2]);
//                    Float zVal = Float.parseFloat(splitString[3]);
//
//                    vertexBuffer.add(xVal);
//                    vertexBuffer.add(yVal);
//                    vertexBuffer.add(zVal);
//
//                    xMinMax[0] = Math.min(xMinMax[0], xVal);
//                    xMinMax[1] = Math.max(xMinMax[1], xVal);
//
//                    yMinMax[0] = Math.min(yMinMax[0], yVal);
//                    yMinMax[1] = Math.max(yMinMax[1], yVal);
//
//                    zMinMax[0] = Math.min(zMinMax[0], zVal);
//                    zMinMax[1] = Math.max(zMinMax[1], zVal);
//
//                    if(yVal < minVal)
//                        minVal = yVal;
//
//                    if(yVal > maxVal)
//                        maxVal = yVal;
//                }
//
//                inputLine = bufferedReader.readLine();
//            }
//
//            float xDistance = xMinMax[1] - xMinMax[0];
//            float yDistance = yMinMax[1] - yMinMax[0];
//            float zDistance = zMinMax[1] - zMinMax[0];
//
//            float height = (maxVal - minVal) / 2.0f + minVal;
//            float distance = Math.max(xDistance, Math.max(yDistance, zDistance));
//
//            output = new float[] { height, distance, xMinMax[0], xMinMax[1], yMinMax[0], yMinMax[1], zMinMax[0], zMinMax[1] };
//        }
//        catch(IOException e)
//        {
//            return output;
//        }
//
//        return output;
//    }
//
//    /**
//     * A private method that reads in from a buffered reader and attempts to populate an
//     * array list with the available normals with each 3 array values in a sequence
//     * representing the x, y, and z values for the normal.
//     * @param bufferedReader	a buffered reader of the currently opened file.
//     * @param normalBuffer		the array list to populate with the normal values.
//     */
//    private static void readNormals(final BufferedReader bufferedReader, String input, ArrayList<Float> normalBuffer)
//    {
//        String inputLine = input;
//        String[] splitString;
//
//        try
//        {
//            if(input.contains("normals"))
//                inputLine = bufferedReader.readLine();
//
//            while(inputLine.startsWith("vn"))
//            {
//                splitString = inputLine.split("\\s+");
//
//                if(splitString.length >= 4)
//                {
//                    normalBuffer.add(Float.parseFloat(splitString[1]));
//                    normalBuffer.add(Float.parseFloat(splitString[2]));
//                    normalBuffer.add(Float.parseFloat(splitString[3]));
//                }
//
//                inputLine = bufferedReader.readLine();
//            }
//        }
//        catch(IOException e)
//        {
//            return;
//        }
//    }
//
//    /**
//     * A private method that reads in from a buffered reader and attempts to populate an
//     * array list with the available texture coordinates with each 2 array values in a
//     * sequence representing the x and y values for the texture coordinate.
//     * @param bufferedReader	a buffered reader of the currently opened file.
//     * @param uvBuffer			the array list to populate with the texture coordinate values.
//     */
//    private static void readUVs(final BufferedReader bufferedReader, String input, ArrayList<Float> uvBuffer)
//    {
//        String inputLine = input;
//        String[] splitString;
//
//        try
//        {
//            if(input.contains("texture"))
//                inputLine = bufferedReader.readLine();
//
//            while(inputLine.startsWith("vt"))
//            {
//                splitString = inputLine.split("\\s+");
//
//                if(splitString.length >= 4)
//                {
//                    uvBuffer.add(Float.parseFloat(splitString[1]));
//                    uvBuffer.add(Float.parseFloat(splitString[2]));
//                }
//
//                inputLine = bufferedReader.readLine();
//            }
//        }
//        catch(IOException e)
//        {
//            Log.e("Polyviewer OBJ UV Error", e.toString());
//        }
//    }
//
//    /**
//     * A private method that takes in a buffered reader of the obj file and array lists for
//     * the vertex, normal and texture coordinate buffers. The method reads in the face data
//     * and obtains the values from each buffer and places them into a new array where the values
//     * are stored for each vertex where every 8 array indices apply to each vertex. The ordering
//     * of the values are the x, y, z values of the vertex followed by the x, y, and z values of
//     * the normal followed by the x and y values of the texture coordinate.
//     *
//     * @param bufferedReader	a buffered reader of the currently opened file.
//     * @param vertexBuffer		the array list to populate with the vertex values.
//     * @param normalBuffer		the array list to populate with the normal values.
//     * @param uvBuffer			the array list to populate with the texture coordinate values.
//     * @return					returns an OGLModel that is empty if the method fails.
//     */
//    private static ObjObject buildObjBuffer(final BufferedReader bufferedReader, final String input, final ArrayList<Float> vertexBuffer,
//                                           final ArrayList<Float> normalBuffer, final ArrayList<Float> uvBuffer)
//    {
//        ObjObject outputModel = new ObjObject();
//        String inputLine = input;
//        String[] splitString;
//        String[] vertexIndices;
//        int vertexIndex;
//        int normalIndex;
//        int uvIndex;
//        short index = 0;
//
//        ArrayList<Float> vertexList = new ArrayList<Float>();
//        ArrayList<Short> indexList = new ArrayList<Short>();
//
//        vertexBuffer.trimToSize();
//        normalBuffer.trimToSize();
//        uvBuffer.trimToSize();
//
//        try
//        {
//            if(input.contains("faces"))
//                inputLine = bufferedReader.readLine();
//
//            while(inputLine.startsWith("f"))
//            {
//                splitString = inputLine.split("\\s+");
//
//                if(splitString.length < 4)
//                    continue;
//
//                for(int i = 1; i < 4; i++)
//                {
//                    vertexIndices = splitString[i].split("/");
//
//                    vertexIndex = Integer.parseInt(vertexIndices[0]);// - 1;
//                    uvIndex = Integer.parseInt(vertexIndices[1]);// - 1;
//                    normalIndex = Integer.parseInt(vertexIndices[2]);// - 1;
//
//                    if(vertexIndex < 0)
//                    {
//                        vertexIndex = vertexBuffer.size() / 3 + vertexIndex;
//                        uvIndex = uvBuffer.size() / 2 + uvIndex;
//                        normalIndex = normalBuffer.size() / 3 + normalIndex;
//                    }
//                    else
//                    {
//                        vertexIndex--;
//                        uvIndex--;
//                        normalIndex--;
//                    }
//
//                    try
//                    {
//                        vertexList.add(vertexBuffer.get(vertexIndex * 3));
//                        vertexList.add(vertexBuffer.get(vertexIndex * 3 + 1));
//                        vertexList.add(vertexBuffer.get(vertexIndex * 3 + 2));
//
//                        vertexList.add(normalBuffer.get(normalIndex * 3));
//                        vertexList.add(normalBuffer.get(normalIndex * 3 + 1));
//                        vertexList.add(normalBuffer.get(normalIndex * 3 + 2));
//
//                        vertexList.add(uvBuffer.get(uvIndex * 2));
//                        vertexList.add((Float)1.0f - uvBuffer.get(uvIndex * 2 + 1));
//
//                        indexList.add(index++);
//                    }
//                    catch(Exception e)
//                    {
//                        break;
//                    }
//                }
//
//                if(splitString.length >= 5)
//                {
//                    int[] indices = new int[] { 1, 3, 4 };
//
//                    for(int value : indices)//int i = 0; i < indices.length; i++)
//                    {
//                        vertexIndices = splitString[value].split("/");
//
//                        vertexIndex = Integer.parseInt(vertexIndices[0]);// - 1;
//                        uvIndex = Integer.parseInt(vertexIndices[1]);// - 1;
//                        normalIndex = Integer.parseInt(vertexIndices[2]);// - 1;
//
//                        if(vertexIndex < 0)
//                        {
//                            vertexIndex = vertexBuffer.size() / 3 + vertexIndex;
//                            uvIndex = uvBuffer.size() / 2 + uvIndex;
//                            normalIndex = normalBuffer.size() / 3 + normalIndex;
//                        }
//                        else
//                        {
//                            vertexIndex--;
//                            uvIndex--;
//                            normalIndex--;
//                        }
//
//                        try
//                        {
//                            vertexList.add(vertexBuffer.get(vertexIndex * 3));
//                            vertexList.add(vertexBuffer.get(vertexIndex * 3 + 1));
//                            vertexList.add(vertexBuffer.get(vertexIndex * 3 + 2));
//
//                            vertexList.add(normalBuffer.get(normalIndex * 3));
//                            vertexList.add(normalBuffer.get(normalIndex * 3 + 1));
//                            vertexList.add(normalBuffer.get(normalIndex * 3 + 2));
//
//                            vertexList.add(uvBuffer.get(uvIndex * 2));
//                            vertexList.add((Float)1.0f - uvBuffer.get(uvIndex * 2 + 1));
//
//                            indexList.add(index++);
//                        }
//                        catch(Exception e)
//                        {
//                            break;
//                        }
//                    }
//                }
//
//                inputLine = bufferedReader.readLine();
//
//                if(inputLine.startsWith("usemtl") || inputLine.startsWith("s"))
//                    inputLine = bufferedReader.readLine();
//            }
//
//            vertexList.trimToSize();
//
//            outputModel.setIndexBuffer(indexList);
//            outputModel.setVertexBuffer(vertexList);
//
//            vertexList.clear();
//        }
//        catch(IOException e)
//        {
//            return null;
//        }
//
//        return outputModel;
//    }
//}