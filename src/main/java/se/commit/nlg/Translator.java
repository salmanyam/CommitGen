package se.commit.nlg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads the translation configuration from a config file and provides the translation
 * @author salman
 *
 */
public class Translator {
    
    private static Map<String, String> mMap;
    
    /**
     * This method returns the translation by a specified type
     * @param type
     * @return a string of translation
     */
    public static String getTranslation(String type) {
        
        if (mMap == null) {
            initializeTranslation();
        }
        
        if (mMap.containsKey(type)) {
            return mMap.get(type);
        }
        
        return type;
    }
        
    /**
     * This method initializes the translation by reading all the translations from a config file
     */
    private static void initializeTranslation() {
        
       mMap = new HashMap<>();
        
       FileReader reader = null;     
        
       String filename = "config/translation.txt";
       String line = "";

       try {
           reader = new FileReader(filename);
           
           try (BufferedReader br = new BufferedReader(reader)) {
               while((line = br.readLine()) != null) {
                   if (line.isEmpty()) continue;
                   
                   String[] items = line.split(",");
                   
                   if (items.length < 2) continue;
                   
                   mMap.put(items[0].trim(), items[1].trim());
                    
               }
           }

       } catch (IOException e) {
           e.printStackTrace();
        
       } finally {
           try {
               if (reader != null)
                   reader.close();

           } catch (IOException ex) {
                ex.printStackTrace();
           }
       }
    }
}
