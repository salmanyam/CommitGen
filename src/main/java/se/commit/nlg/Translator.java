package se.commit.nlg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public class Translator {
    
    public static String getTranslationByType(String type, ChangeType changeType) {
        
        if (changeType == ChangeType.DELETE) {
            return DeletionTranslator.getTranslationByType(type);
        }
        return AddOrUpdateTranslator.getTranslationByType(type);
        
    }
    
    public static String getMethodPrepositionByType(String type, ChangeType changeType) {
        if (changeType == ChangeType.DELETE) {
            return DeletionTranslator.getMethodPrepositionByType(type);
        }
        return AddOrUpdateTranslator.getMethodPrepositionByType(type);
        
    }
    
    public static String getClassPrepositionByType(String type, ChangeType changeType) {
        
        if (changeType == ChangeType.DELETE) {
            return DeletionTranslator.getClassPrepositionByType(type);
        }
        return AddOrUpdateTranslator.getClassPrepositionByType(type);
    }
        
    public static void initializeTranslation() {
        
       AddOrUpdateTranslator.initialize();
       DeletionTranslator.initialize();
        
       FileReader reader = null;     
        
       String filename = "data//translation.txt";
       String line = "";

       try {
           reader = new FileReader(filename);
           
           try (BufferedReader br = new BufferedReader(reader)) {
               while((line = br.readLine()) != null) {
                   if (line.isEmpty()) continue;
                   
                   String[] items = line.split(",");
                   
                   if (items.length < 5) continue;
                   
                   if (items[0].trim().equals("addOrUpdate")) {
                       AddOrUpdateTranslator.add(items[1].trim(), items[2].trim(), items[3].trim(), items[4].trim());
                    
                   } else {
                       DeletionTranslator.add(items[1].trim(), items[2].trim(), items[3].trim(), items[4].trim());
                    
                   }
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
