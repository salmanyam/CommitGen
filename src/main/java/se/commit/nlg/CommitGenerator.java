package se.commit.nlg;

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
import com.google.common.io.Files;

import se.commit.jgit.Commit;
import se.commit.jgit.JGitWrapper;
import se.commit.utils.Utils;

/**
 * This class contains some helper functionalities to test the NLG unit
 * @author salman
 *
 */
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
    
    /**
     * This method generate the change description for the difference between the head of a Git repository to the lastest stage.
     */
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
    
    /**
     * This method generates the change description for a specified revisions
     * @param commitData : all commit information
     * @param revNo : revision number
     */
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
    
    
    /**
     * This method determines whether a revision is discarded based on the number of changed files.
     * @param diffs : contains the number of changed files and change types
     * @return true or false
     */
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
}
