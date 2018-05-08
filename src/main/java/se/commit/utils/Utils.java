package se.commit.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * This class contains the utility methods
 * @author salman
 *
 */
public class Utils {
    
	/**
	 * This method creates a temporary with specified contents
	 * @param fileName : file path
	 * @param data : data as the file contents
	 * @return a file
	 */
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
    
    /**
     * This method converts a date to string
     * @param date
     * @return string
     */
    public static String toDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        
        return cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR);
        
    }
}
