package se.commit.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

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
    
    public static String toDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        
        return cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR);
        
    }
}
