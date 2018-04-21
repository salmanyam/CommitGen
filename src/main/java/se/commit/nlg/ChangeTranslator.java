package se.commit.nlg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChangeTranslator {
    private String type;
    private String translation;
    private static Map<String, String> mMap;

    private ChangeTranslator(String type, String translation) {
        this.type = type;
        this.translation = translation;
    }
    public String getType() {
        return type;
    }

    public String getTranslation() {
        return translation;
    }


    public static String getTranslationByTode(String type) {
        if (mMap == null) {
            initializeTranslation();
        }
        if (mMap.containsKey(type)) {
            return mMap.get(type);
        }
        return type;
    }

    private static void initializeTranslation() {
        mMap = new HashMap<>();
        FileReader reader = null;     
        
        String filename = "data//translation.txt";
        String line = "";
        try {
            reader = new FileReader(filename);
            try (BufferedReader br = new BufferedReader(reader)) {
                while((line = br.readLine()) != null) {
                    String[] items = line.split(",");
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
