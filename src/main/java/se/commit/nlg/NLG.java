package se.commit.nlg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

import com.github.gumtreediff.client.diff.ChangeData;

public class NLG {
    
    public static String generateChangeSentence(Set<ChangeData> changeItems, ChangeType changeType) {
        System.out.println(strChangeSentence(changeItems, changeType));
        
        String operation = null;
        String operationIng = null;
        if (changeType == ChangeType.ADD) {
            operation = "added";
            operationIng = "adding";
        } else if (changeType == ChangeType.MODIFY) {
                operation = "updated";
                operationIng = "modifying";
        } else {
            operation = "deleted";
            operationIng = "removing";
        }
                
        
        Set<String> classes = new HashSet<>();
        String sentence = "";
        String sentences = "";
        
        for (ChangeData data : changeItems) {
            String [] items = data.getType().split(":");
            String type = items[0].trim();
            
            if (type.equals("TypeDeclaration")) {
                classes.add(data.getClassName());
            
            } 
        }
        
        int start = 0;
        if (classes.size() > 0) {
            if (classes.size() > 1)
                sentences += operation + " classes ";
            else
                sentences += operation + " class ";
            
            start = classes.size();
            for (String name : classes) {
                if (classes.size() == 1) sentences += name;
                else if (start == 1) sentences += "and " + name;
                else sentences += name + ", ";
                start--;
            }
            sentences += ". ";
        }
        
        
        Map<String, Map<String, List<ChangeData>>> methodMap = changeItems.stream().collect(Collectors.groupingBy(ChangeData::getClassName, Collectors.groupingBy(ChangeData::getMethodName)));
        Map<String, Map<String, List<ChangeData>>> fieldMap = changeItems.stream().collect(Collectors.groupingBy(ChangeData::getClassName, Collectors.groupingBy(ChangeData::getFieldName)));
        
        Set<String> changes = new HashSet<>();
        
        for (Map.Entry<String, Map<String, List<ChangeData>>> entry: methodMap.entrySet()) {

            Map<String, List<ChangeData>> mymap = entry.getValue();
            
            for (Map.Entry<String, List<ChangeData>> entry2: mymap.entrySet()) {
                //System.out.println(entry2.getKey());
                
                if (entry2.getKey().trim().isEmpty())
                    continue;
                
                List<ChangeData> mylist = entry2.getValue();

                Set<String> types = new HashSet<>();
                
                for (ChangeData myitem : mylist) {
                    //System.out.println(myitem.toString());
                    String type = myitem.getType().split(":")[0].trim();
                    types.add(type);
                }
                if (types.size() > 0) {
               
                    sentence = "updated " + entry2.getKey() + " method in " + entry.getKey() + " by " + operationIng + " ";
                    int index = types.size();
                
                    for (String item : types) {
                        if (types.size() == 1) sentence += Translator.getTranslation(item);
                        else if (index == 1) sentence += "and " + Translator.getTranslation(item);
                        else sentence += Translator.getTranslation(item) + ", ";
                        index--;
                    }
                    
                    changes.add(sentence);
                }
            }
        }
       
        //System.out.println("Starting field here");
        
        for (Map.Entry<String, Map<String, List<ChangeData>>> entry: fieldMap.entrySet()) {

            Map<String, List<ChangeData>> mymap = entry.getValue();
            Set<String> fields = new HashSet<>();
            Set<String> fieldUpdates = new HashSet<>();
            
            for (Map.Entry<String, List<ChangeData>> entry2: mymap.entrySet()) {
                //System.out.println("field: " + entry2.getKey());
                
                if (entry2.getKey().trim().isEmpty())
                    continue;
                
                List<ChangeData> mylist = entry2.getValue();
                
                Set<String> types = new HashSet<>();
                
                
                for (ChangeData myitem : mylist) {
                    //System.out.println(myitem.toString());
                    String type = myitem.getType().split(":")[0].trim();
                    types.add(type+":"+myitem.getFieldName());
                    
                }
                
                if (types.size() > 0) {
                    for (String item : types) {
                        String[] r = item.split(":");
                        if (r.length < 2) continue;
                        
                        if (r[0].trim().equals("FieldDeclaration")) {
                            fields.add(r[1].trim());  
                        } else {
                            fieldUpdates.add(r[1].trim());
                        }
                    }
                }
            }
            
            if (fields.size() > 0) {
                sentence = "";
                
                if (fields.size() > 1)
                    sentence = operation + " attributes";
                else
                    sentence = operation + " attribute ";
                
                int index = fields.size();
                
                for (String item : fields) {
                    
                    if (fields.size() == 1) sentence += item;
                    else if (index == 1) sentence += "and " + item;
                    else sentence += item + ", ";
                    index--;
                }
                
                //System.out.println(sentence);
                changes.add(sentence);
            }
            if (fieldUpdates.size() > 0) {
                sentence = "";
                if (fieldUpdates.size() > 1)
                    sentence = "update attributes ";
                else
                    sentence = "update attribute ";
                
                int index = fieldUpdates.size();
                
                for (String item : fieldUpdates) {
                    
                    if (fieldUpdates.size() == 1) sentence += item;
                    else if (index == 1) sentence += "and " + item;
                    else sentence += item + ", ";
                    index--;
                }
                
                //System.out.println(sentence);
                changes.add(sentence);
            }
        }
        
        //System.out.println("Ending field here");
        
        for (String item : changes) {
            //System.out.println(item);
            sentences += item + ". ";
        }
        
        /*
        
        String sentences = "";
       
        Set<String> classes = new HashSet<>();
        Set<String> lines = new HashSet<>();
        
        for (ChangeData data : changeItems) {
            String [] items = data.getType().split(":");
            String type = items[0].trim();
            String name = null;
            if (items.length > 1) 
                name = items[1].trim();
            
            if (type.equals("TypeDeclaration")) {
                classes.add(data.getClassName());
            
            } else {
                //when type has name with it
                if (items.length > 1) {
                    if (data.getMethodName().isEmpty()) {
                        
                        lines.add(
                                //name + " " + 
                                Translator.getTranslationByType(type, changeType) + " " + 
                                Translator.getClassPrepositionByType(type, changeType) + " " +
                                data.getClassName() + " class");
                                              
                    } else {
                        lines.add(
                                //name + " " + 
                                Translator.getTranslationByType(type, changeType) + " " +
                                Translator.getMethodPrepositionByType(type, changeType) + " " +
                                data.getMethodName() + " method " + 
                                Translator.getClassPrepositionByType(type, changeType) + " " +
                                data.getClassName() + " class");
                    }
                    
                } else { //No name, only type
                    if (data.getMethodName().isEmpty()) {
                        lines.add(
                                Translator.getTranslationByType(type, changeType) + " " + 
                                Translator.getClassPrepositionByType(type, changeType) + " " +
                                data.getClassName() + " class");
                    } else {
                        lines.add(
                                Translator.getTranslationByType(type, changeType) + " " +
                                Translator.getMethodPrepositionByType(type, changeType) + " " +
                                data.getMethodName() + " method " + 
                                Translator.getClassPrepositionByType(type, changeType) + " " +
                                data.getClassName() + " class");
                    }
                }
            }
        }
        
        int start = 0;
        if (classes.size() > 0) {
            sentences += operation + " class ";
            start = classes.size();
            for (String name : classes) {
                if (classes.size() == 1) sentences += name;
                else if (start == 1) sentences += "and " + name;
                else sentences += name + ", ";
                start--;
            }
        }
        
        //System.out.println(lines.size());
        //System.out.println(lines);
        
        if (lines.size() > 0) {
            //sentences += operation + " ";
            start = lines.size();
            for (String line : lines) {
                if (lines.size() == 1) sentences += operation + " "+ line;
                else if (start == 1) sentences += "and " + operation + " "+ line;
                else sentences += operation + " "+ line + ", ";
                start--;
            }
        }        */
        
        //System.out.println(sentences);
        
        return sentences;
    }
    
    public static String strChangeSentence(Set<ChangeData> insertItems, ChangeType changeType) {
        
        String operation = null;
        String operationIng = null;
        if (changeType == ChangeType.ADD) {
            operation = "added";
            operationIng = "adding";
        } else if (changeType == ChangeType.MODIFY) {
                operation = "updated";
                operationIng = "modifying";
        } else {
            operation = "deleted";
            operationIng = "removing";
        }
        
        String result = operation + " Items\n=========================\n";
        Iterator<ChangeData> iter = insertItems.iterator();
        
        while(iter.hasNext()) {
            result += iter.next().toString() + "\n";
        }
        
        return result;
    }
    
    public static String strInsertSentence(Set<ChangeData> insertItems) {
        String result = "Inserted Items\n=========================\n";
        Iterator<ChangeData> iter = insertItems.iterator();
        
        while(iter.hasNext()) {
            result += iter.next().toString() + "\n";
        }
        
        return result;
    }

    public static String strUpdateSentence(Set<ChangeData> updateItems) {
        String result = "Updated Items\n=========================\n";
        Iterator<ChangeData> iter = updateItems.iterator();
        
        while(iter.hasNext()) {
            result += iter.next().toString() + "\n";
        }
        
        return result;
    }
    
    public static String strDeleteSentence(Set<ChangeData> deletedItems) {
        String result = "Deleted Items\n=========================\n";
        Iterator<ChangeData> iter = deletedItems.iterator();
        
        while(iter.hasNext()) {
            result += iter.next().toString() + "\n";
        }
        
        return result;
    }
}
