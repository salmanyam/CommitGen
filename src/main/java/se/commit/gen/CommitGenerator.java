package se.commit.gen;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.client.diff.CommitGen;
import com.github.gumtreediff.utils.Tuple;

public class CommitGenerator {
    
    private Set<Tuple<String, String, String>> insertedItems = null;
    private Set<Tuple<String, String, String>> updatedItems = null;
    private Set<Tuple<String, String, String>> deletedItems = null;
    
    private String gitRepoName = "";
    
    public CommitGenerator(String repoName) {
        this.insertedItems = new HashSet<>();
        this.updatedItems = new HashSet<>();
        this.deletedItems = new HashSet<>();
        
        this.gitRepoName = repoName;
    }
    
    public void generate() {
        try {
            try (Repository repository = JGitWrapper.openGitRepository(this.gitRepoName)){
                
                Set<String> modifiedFiles = JGitWrapper.getModifiedFiles(repository);
                
                if (modifiedFiles == null) return;
                
                for (String filename : modifiedFiles) {
                    
                    File srcFile = Utils.createTempFile(filename, JGitWrapper.getFileContentFromRecentCommit(filename, repository));
                    File destFile = new File(repository.getDirectory().getParentFile(), filename);
                    
                    Run.initGenerators();
                    CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), destFile.getAbsolutePath()});
                    jd.run();
                    
                    Set<Tuple<String, String, String>> insItems = jd.getInsertedItems();
                    for (Tuple<String, String, String> pair : insItems)
                        insertedItems.add(pair);
                    
                    Set<Tuple<String, String, String>> updItems = jd.getUpdatedItems();
                    for (Tuple<String, String, String> pair : updItems)
                        updatedItems.add(pair);
                    
                    Set<Tuple<String, String, String>> delItems = jd.getDeletedItems();
                    for (Tuple<String, String, String> pair : delItems)
                        deletedItems.add(pair);
                }
            }
            
            System.out.print(generateInsertSentence(insertedItems));
            System.out.println(generateUpdateSentence(updatedItems));
 
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    public static String generateInsertSentence(Set<Tuple<String, String, String>> insertItems) {
        String sentences = "";
        Map<String, Set<String>> propertyMaps = new HashMap<>();
        Set<String> classes = new HashSet<>();
        for (Tuple<String, String, String> tuple : insertItems) {
            Set<String> hs = propertyMaps.get(tuple.third);
            if (hs == null) {
                hs = new HashSet<>();
            }
            if (tuple.first.equalsIgnoreCase("SingleVariableDeclaration") 
                    && tuple.second.equalsIgnoreCase("MethodDeclaration")) {
                hs.add("method parameters");
            } else if (tuple.first.equalsIgnoreCase("FieldDeclaration") 
                    && tuple.second.equalsIgnoreCase("TypeDeclaration")) {
                hs.add("attributes");
            } else if (tuple.first.equalsIgnoreCase("TypeDeclaration") 
                    && tuple.second.equalsIgnoreCase("CompilationUnit")) {
                classes.add(tuple.third);
            }
            propertyMaps.put(tuple.third, hs);
        }
        int start = 0;
        int end = 0;
        if (classes.size() > 0) {
            sentences += "Added " + classes.size() + " classes named ";
            start = 0;
            for (String name : classes) {
                sentences += name;
                if (start < classes.size() - 2) sentences += ", ";
                if (start < classes.size() - 1) sentences += ", and ";
                start++;
            }
        }
        if (propertyMaps.size() > 0)sentences += ";";
        end = 0;
        for (Map.Entry<String, Set<String>> entry : propertyMaps.entrySet()) {
            Set<String> params = entry.getValue();
            if (params.size() == 0) continue;
            end++;
            String line = " added ";
            if (end > 1) line = "; added ";
            start = 0;
            for (String name : params) {
                line += name;
                if (start < params.size() - 1) line += ", ";
                start++;
            }
            
            sentences += line + " in " + entry.getKey() + " class";
            
        }
        return sentences;
    }

    public static String generateUpdateSentence(Set<Tuple<String, String, String>> updateItems) {
        String sentences = "";
        Map<String, Set<String>> propertyMaps = new HashMap<>();
        
        for (Tuple<String, String, String> tuple : updateItems) {
            Set<String> hs = propertyMaps.get(tuple.third);
            if (hs == null) {
                hs = new HashSet<>();
            }
            if (tuple.first.contains("SimpleName") 
                    && tuple.second.equalsIgnoreCase("MethodDeclaration")) {
                hs.add("method name");
            } else if (tuple.first.equalsIgnoreCase("FieldDeclaration") 
                    && tuple.second.equalsIgnoreCase("TypeDeclaration")) {
                //hs.add("attributes");
            } 
            propertyMaps.put(tuple.third, hs);
        }
        int start = 0;
        int end = 0;
        if (propertyMaps.size() > 0)sentences += ";";
        end = 0;
        for (Map.Entry<String, Set<String>> entry : propertyMaps.entrySet()) {
            Set<String> params = entry.getValue();
            if (params.size() == 0) continue;
            end++;
            String line = " updated ";
            if (end > 1) line = "; updated ";
            start = 0;
            for (String name : params) {
                line += name;
                if (start < params.size() - 1) line += ", ";
                start++;
            }
            
            sentences += line + " in " + entry.getKey() + " class";
            
        }
        return sentences;
    }

}
