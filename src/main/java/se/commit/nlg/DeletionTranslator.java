package se.commit.nlg;

import java.util.HashMap;
import java.util.Map;

public class DeletionTranslator {
    private static Map<String, String> tMap;
    private static Map<String, String> mMap;
    private static Map<String, String> cMap;


    public static void initialize() {
        tMap = new HashMap<>();
        mMap = new HashMap<>();
        cMap = new HashMap<>();
    }
    
    public static void add(String type, String desc, String mPrep, String cPrep) {
        tMap.put(type, desc);
        mMap.put(type, mPrep);
        cMap.put(type, cPrep);
    }
    
    public static String getTranslationByType(String type) {
        if (tMap == null) {
            return type;
        }
        if (tMap.containsKey(type)) {
            return tMap.get(type);
        }
        return type;
    }
    
    public static String getMethodPrepositionByType(String type) {
        if (mMap == null) {
            return "";
        }
        if (mMap.containsKey(type)) {
            return mMap.get(type);
        }
        return "";
    }
    
    public static String getClassPrepositionByType(String type) {
        if (cMap == null) {
            return "";
        }
        if (cMap.containsKey(type)) {
            return cMap.get(type);
        }
        return "";
    }
}
