package zju.homework.augmentedstudio.Utils.Tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import zju.homework.augmentedstudio.Models.Material;

/**
 * Created by stardust on 2017/1/2.
 */


public class MTLReader {


    public  static Vector<Material> loadMTL(String file){
        BufferedReader reader=null;
        Vector<Material> materials=new Vector<Material>();
        String line;
        Material currentMtl=null;
        try { //try to open file
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch(IOException e){
            e.printStackTrace();
        }
        if(reader!=null){
            try {//try to read lines of the file
                while((line = reader.readLine()) != null) {
                    if(line.startsWith("newmtl")){
                        String mtName = line.split("[ ]+", 2)[1];
                        currentMtl = new Material(mtName);
                        materials.add(currentMtl);
                    }
                    else
                    if(line.startsWith("Ka")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setAmbientColor(Float.parseFloat(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
                    }
                    else
                    if(line.startsWith("Kd")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setDiffuseColor(Float.parseFloat(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
                    }
                    else
                    if(line.startsWith("Ks")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setSpecularColor(Float.parseFloat(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
                    }
                    else
                    if(line.startsWith("Tr") || line.startsWith("d")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setAlpha(Float.parseFloat(str[1]));
                    }
                    else
                    if(line.startsWith("Ns")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setShine(Float.parseFloat(str[1]));
                    }
                    else
                    if(line.startsWith("illum")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setIllum(Integer.parseInt(str[1]));
                    }
                    else
                    if(line.startsWith("map_Ka") || line.startsWith("map_Kd") || line.startsWith("map_Ks")){
                        String[] str=line.split("[ ]+");
                        currentMtl.setTextureFile(file.substring(0, file.lastIndexOf('/'))+"/"+str[1]);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return materials;
    }
}
