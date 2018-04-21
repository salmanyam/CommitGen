package se.commit.gen;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.client.diff.ChangeData;
import com.github.gumtreediff.client.diff.CommitGen;
import com.github.gumtreediff.client.diff.swing.SwingDiff;
import com.github.gumtreediff.utils.Tuple;
import com.google.common.io.Files;

import se.commit.jgit.Commit;
import se.commit.jgit.JGitWrapper;
import se.commit.utils.Utils;

public class CommitGenerator {
    
    private Set<ChangeData> insertedItems = null;
    private Set<ChangeData> updatedItems = null;
    private Set<ChangeData> deletedItems = null;
    
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
                    
                    /*Set<Tuple<String, String, String>> insItems = jd.getInsertedItems();
                    for (Tuple<String, String, String> pair : insItems)
                        insertedItems.add(pair);
                    
                    Set<Tuple<String, String, String>> updItems = jd.getUpdatedItems();
                    for (Tuple<String, String, String> pair : updItems)
                        updatedItems.add(pair);
                    
                    Set<Tuple<String, String, String>> delItems = jd.getDeletedItems();
                    for (Tuple<String, String, String> pair : delItems)
                        deletedItems.add(pair);*/
                }
            }
            
            //System.out.println(generateInsertSentence(insertedItems));
            //System.out.println(generateUpdateSentence(updatedItems));
            //System.out.println(generateDeleteSentence(deletedItems));
 
        } catch(Exception ex) {
            ex.printStackTrace();
        }     
    }
    
    public void generateGUI(List<Commit> commitData, int revNo) {
        try {
            try (Repository repository = JGitWrapper.openGitRepository(this.gitRepoName)){
                
                List<DiffEntry> diffs = JGitWrapper.listDiff(
                        repository,
                        commitData.get(revNo-1).getRevision(),
                        commitData.get(revNo).getRevision());
                
                if (!isDiscardCommit(diffs)) {

                    SwingDiff[] jd = new SwingDiff[diffs.size()];
                    int gc = 0;
                    
                    File srcFile = null;
                    File dstFile = null;
                            
                    for (DiffEntry diff : diffs) {
                        
                        if (diff.getChangeType() == ChangeType.ADD) {
                            
                            if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                                continue;
                            
                            srcFile = Utils.createTempFile("dummy", "");
                            dstFile = Utils.createTempFile(
                                    diff.getNewPath(), 
                                    JGitWrapper.getFileContentFromRevision(
                                            diff.getNewPath(), 
                                            repository, 
                                            commitData.get(revNo).getRevision()));
   
                        } else if (diff.getChangeType() == ChangeType.DELETE) {
                            
                            if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                                continue;
                            
                            srcFile = Utils.createTempFile(
                                    "dummy", 
                                    JGitWrapper.getFileContentFromRevision(
                                            diff.getOldPath(), 
                                            repository, 
                                            commitData.get(revNo-1).getRevision()));
                            
                            dstFile = Utils.createTempFile("dummy", "");
                            
                        } else if (diff.getChangeType() == ChangeType.MODIFY) {
                            
                            if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                                continue;
                            
                            srcFile = Utils.createTempFile(
                                    "dummy", 
                                    JGitWrapper.getFileContentFromRevision(
                                            diff.getNewPath(), 
                                            repository, 
                                            commitData.get(revNo-1).getRevision()));
                            
                            dstFile = Utils.createTempFile(
                                    "dummy", 
                                    JGitWrapper.getFileContentFromRevision(
                                            diff.getNewPath(), 
                                            repository, 
                                            commitData.get(revNo).getRevision()));
                        } 
                        if (!srcFile.exists() || !dstFile.exists())
                            continue;
                        
                        Run.initGenerators();
                        jd[gc] = new SwingDiff(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                        jd[gc++].run();
                        
                    }
                }
            }
 
        } catch(Exception ex) {
            ex.printStackTrace();
        }   
    }
    
    public static boolean isDiscardCommit(List<DiffEntry> diffs) {
        
        int added = 0, modified = 0, deleted = 0;
        int total = 0;
        
        if (diffs.size() <= 0)
            return true;
        
        for (DiffEntry diff : diffs) {
                        
            if (diff.getChangeType() == ChangeType.ADD) {
                
                if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                    continue;
                
                added++;
                
            } else if (diff.getChangeType() == ChangeType.MODIFY) {
                
                if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                    continue;
                
                modified++;
                
            } else if (diff.getChangeType() == ChangeType.DELETE) {
                
                if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                    continue;
                
                deleted++;
                
            } else {
                continue;
            }           
        }
        
        total = added + modified + deleted;
        
        if (total <= 0) return true;
        if (modified > 10) return true;
        if (total > 15) return true;
                
        return false;
    }
    
    public static String generateInsertSentence(Set<ChangeData> insertItems) {
        System.out.println("Inserted Items\n=========================");
        Iterator<ChangeData> iter = insertItems.iterator();
        while(iter.hasNext()) {
            System.out.print(iter.next().toString() + " ");
        }
        System.out.println("");
        
        String sentences = "";
        Map<String, Set<String>> propertyMaps = new HashMap<>();
        Set<String> classes = new HashSet<>();
        /*for (ChangeData tuple : insertItems) {
            Set<String> hs = propertyMaps.get(tuple.getClassName());
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
            sentences += "Added class ";
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
            
        }*/
        return sentences;
    }

    public static String generateUpdateSentence(Set<ChangeData> updateItems) {
        System.out.println("Updated Items\n=========================");
        Iterator<ChangeData> iter = updateItems.iterator();
        while(iter.hasNext()) {
            System.out.print(iter.next().toString() + " ");
        }
        System.out.println("");
        
        String sentences = "";
        Map<String, Set<String>> propertyMaps = new HashMap<>();
        /*
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
            
        }*/
        return sentences;
    }
    
    public static String generateDeleteSentence(Set<ChangeData> deletedItems) {
        System.out.println("Deleted Items\n=========================");
        Iterator<ChangeData> iter = deletedItems.iterator();
        while(iter.hasNext()) {
            System.out.print(iter.next().toString() + " ");
        }
        System.out.println("");

        String sentences = "";
        Map<String, Set<String>> propertyMaps = new HashMap<>();
        Set<String> classes = new HashSet<>();
        /*for (Tuple<String, String, String> tuple : deletedItems) {
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
            sentences += "Deleted class ";
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
            String line = " deleted ";
            if (end > 1) line = "; deleted ";
            start = 0;
            for (String name : params) {
                line += name;
                if (start < params.size() - 1) line += ", ";
                start++;
            }
            
            sentences += line + " in " + entry.getKey() + " class";
            
        }*/
        return sentences;
    }
}
