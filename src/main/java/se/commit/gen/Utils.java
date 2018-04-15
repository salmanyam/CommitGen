package se.commit.gen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {
    
    public static File createTempFile(String fileName, String data) {
        File tmpFile = null;
        
        try {
            
            tmpFile = File.createTempFile("Temp"+ fileName, ".java"); 
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile.getAbsoluteFile()))){
                writer.write(data);
                writer.close();
            }
    
        } catch (IOException ex) {
            
            ex.printStackTrace();
        }
        
        return tmpFile;
    }
}
