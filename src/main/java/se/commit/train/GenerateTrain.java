package se.commit.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.client.diff.ChangeData;
import com.github.gumtreediff.client.diff.CommitGen;
import com.google.common.io.Files;

import se.commit.jgit.Commit;
import se.commit.jgit.JGitWrapper;
import se.commit.nlg.NLG;
import se.commit.utils.Utils;

public class GenerateTrain {
    
    private String repoName;
    
    public GenerateTrain(String repoName) {
        this.repoName = repoName;
    }
    
    public void generate(String filename) {
        try(Repository repository = JGitWrapper.openGitRepository(repoName)) {
            List<Commit> commitData = JGitWrapper.getAllCommits(repository);
            Collections.reverse(commitData);
            
            FileWriter fw = null;           
            
            try {
                fw = new FileWriter(filename);
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    for (int revNo = 2; revNo < commitData.size(); revNo++) {
                        
                        List<DiffEntry> diffs = JGitWrapper.listDiff(
                                repository,
                                commitData.get(revNo-1).getRevision(),
                                commitData.get(revNo).getRevision());
                        
                        //bw.write("RevNo: " + revNo + ", Rev: " + commitData.get(revNo).getRevision() + ", Changes: " + diffs.size() + "\n");
                        //bw.write("Commit: " + commitData.get(revNo).getMessage() + "\n");
                        bw.write(revNo + "\t");
                        bw.write(commitData.get(revNo).getRevision() + "\t");
                        bw.write(commitData.get(revNo).getMessage() + "\t");
                        
                        if (!isDiscardCommit(diffs)) {
                            
                            Set<ChangeData> insertedItems = new HashSet<>();
                            Set<ChangeData> updatedItems = new HashSet<>();
                            Set<ChangeData> deletedItems = new HashSet<>();
                            
                            for (DiffEntry diff : diffs) {
                                
                                if (diff.getChangeType() == ChangeType.ADD) {
                                    
                                    if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                                        continue;
                                    
                                    File srcFile = Utils.createTempFile("dummy", "");
                                    File dstFile = Utils.createTempFile(
                                            diff.getNewPath(), 
                                            JGitWrapper.getFileContentFromRevision(
                                                    diff.getNewPath(), 
                                                    repository, 
                                                    commitData.get(revNo).getRevision()));
                                    
                                    
                                    if (!srcFile.exists() || !dstFile.exists())
                                        continue;
                                    
                                    //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                                    
                                    Run.initGenerators();
                                    CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                    jd.run();
                                    
                                    Set<ChangeData> insItems = jd.getInsertedItems();
                                    for (ChangeData pair : insItems)
                                        insertedItems.add(pair);
                                    Set<ChangeData> updItems = jd.getUpdatedItems();
                                    for (ChangeData pair : updItems)
                                        updatedItems.add(pair);
                                    Set<ChangeData> delItems = jd.getDeletedItems();
                                    for (ChangeData pair : delItems)
                                        deletedItems.add(pair);
                                    
                                } else if (diff.getChangeType() == ChangeType.DELETE) {
                                    
                                    if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                                        continue;
                                    
                                    File srcFile = Utils.createTempFile(
                                            "dummy", 
                                            JGitWrapper.getFileContentFromRevision(
                                                    diff.getOldPath(), 
                                                    repository, 
                                                    commitData.get(revNo-1).getRevision()));
                                    
                                    File dstFile = Utils.createTempFile("dummy", "");
                                    
                                    
                                    if (!srcFile.exists() || !dstFile.exists())
                                        continue;
                                    
                                    //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                                    
                                    Run.initGenerators();
                                    CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                    jd.run();
                                    
                                    Set<ChangeData> insItems = jd.getInsertedItems();
                                    for (ChangeData pair : insItems)
                                        insertedItems.add(pair);
                                    Set<ChangeData> updItems = jd.getUpdatedItems();
                                    for (ChangeData pair : updItems)
                                        updatedItems.add(pair);
                                    Set<ChangeData> delItems = jd.getDeletedItems();
                                    for (ChangeData pair : delItems)
                                        deletedItems.add(pair);
                                    
                                } else if (diff.getChangeType() == ChangeType.MODIFY) {
                                    
                                    if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                                        continue;
                                    
                                    File srcFile = Utils.createTempFile(
                                            "dummy", 
                                            JGitWrapper.getFileContentFromRevision(
                                                    diff.getNewPath(), 
                                                    repository, 
                                                    commitData.get(revNo-1).getRevision()));
                                    
                                    File dstFile = Utils.createTempFile(
                                            "dummy", 
                                            JGitWrapper.getFileContentFromRevision(
                                                    diff.getNewPath(), 
                                                    repository, 
                                                    commitData.get(revNo).getRevision()));
                                    
                                    //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                                    
                                    if (!srcFile.exists() || !dstFile.exists())
                                        continue;
                                    
                                    Run.initGenerators();
                                    CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                    jd.run();
                                    
                                    Set<ChangeData> insItems = jd.getInsertedItems();
                                    for (ChangeData pair : insItems)
                                        insertedItems.add(pair);
                                    Set<ChangeData> updItems = jd.getUpdatedItems();
                                    for (ChangeData pair : updItems)
                                        updatedItems.add(pair);
                                    Set<ChangeData> delItems = jd.getDeletedItems();
                                    for (ChangeData pair : delItems)
                                        deletedItems.add(pair);
                                } 
                            }
                            String result = NLG.generateChangeSentence(insertedItems, ChangeType.ADD);
                            if (!result.isEmpty()) bw.write(result + ". ");
                            result = NLG.generateChangeSentence(updatedItems, ChangeType.MODIFY);
                            if (!result.isEmpty()) bw.write(result + ". ");
                            result = NLG.generateChangeSentence(deletedItems, ChangeType.DELETE);
                            if (!result.isEmpty()) bw.write(result + ". ");
                            
                        } else {
                            bw.write("Discarded");
                        }
                        bw.write("\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (fw != null)
                        fw.close();
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    public void generate(int revNo) {
        try(Repository repository = JGitWrapper.openGitRepository(repoName)) {
            List<Commit> commitData = JGitWrapper.getAllCommits(repository);
            Collections.reverse(commitData);
            
            String filename = "data//change_data_single.txt";
            
            FileWriter fw = null;           
            
            try {
                fw = new FileWriter(filename);
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    List<DiffEntry> diffs = JGitWrapper.listDiff(
                            repository,
                            commitData.get(revNo-1).getRevision(),
                            commitData.get(revNo).getRevision());
                    
                    bw.write("RevNo: " + revNo + ", Rev: " + commitData.get(revNo).getRevision() + ", Changes: " + diffs.size() + "\n");
                    bw.write("Commit: " + commitData.get(revNo).getMessage() + "\n");
                    
                    if (!isDiscardCommit(diffs)) {
                        
                        Set<ChangeData> insertedItems = new HashSet<>();
                        Set<ChangeData> updatedItems = new HashSet<>();
                        Set<ChangeData> deletedItems = new HashSet<>();
                        
                        for (DiffEntry diff : diffs) {
                            
                            if (diff.getChangeType() == ChangeType.ADD) {
                                
                                if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                                    continue;
                                
                                File srcFile = Utils.createTempFile("dummy", "");
                                File dstFile = Utils.createTempFile(
                                        diff.getNewPath(), 
                                        JGitWrapper.getFileContentFromRevision(
                                                diff.getNewPath(), 
                                                repository, 
                                                commitData.get(revNo).getRevision()));
                                
                                
                                if (!srcFile.exists() || !dstFile.exists())
                                    continue;
                                
                                //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                                
                                Run.initGenerators();
                                CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                jd.run();
                                
                                Set<ChangeData> insItems = jd.getInsertedItems();
                                for (ChangeData pair : insItems)
                                    insertedItems.add(pair);
                                Set<ChangeData> updItems = jd.getUpdatedItems();
                                for (ChangeData pair : updItems)
                                    updatedItems.add(pair);
                                Set<ChangeData> delItems = jd.getDeletedItems();
                                for (ChangeData pair : delItems)
                                    deletedItems.add(pair);
                                
                            } else if (diff.getChangeType() == ChangeType.DELETE) {
                                
                                if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                                    continue;
                                
                                File srcFile = Utils.createTempFile(
                                        "dummy", 
                                        JGitWrapper.getFileContentFromRevision(
                                                diff.getOldPath(), 
                                                repository, 
                                                commitData.get(revNo-1).getRevision()));
                                
                                File dstFile = Utils.createTempFile("dummy", "");
                                
                                
                                if (!srcFile.exists() || !dstFile.exists())
                                    continue;
                                
                                //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                                
                                Run.initGenerators();
                                CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                jd.run();
                                
                                Set<ChangeData> insItems = jd.getInsertedItems();
                                for (ChangeData pair : insItems)
                                    insertedItems.add(pair);
                                Set<ChangeData> updItems = jd.getUpdatedItems();
                                for (ChangeData pair : updItems)
                                    updatedItems.add(pair);
                                Set<ChangeData> delItems = jd.getDeletedItems();
                                for (ChangeData pair : delItems)
                                    deletedItems.add(pair);
                                
                            } else if (diff.getChangeType() == ChangeType.MODIFY) {
                                
                                if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                                    continue;
                                
                                File srcFile = Utils.createTempFile(
                                        "dummy", 
                                        JGitWrapper.getFileContentFromRevision(
                                                diff.getNewPath(), 
                                                repository, 
                                                commitData.get(revNo-1).getRevision()));
                                
                                File dstFile = Utils.createTempFile(
                                        "dummy", 
                                        JGitWrapper.getFileContentFromRevision(
                                                diff.getNewPath(), 
                                                repository, 
                                                commitData.get(revNo).getRevision()));
                                
                                //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                                
                                if (!srcFile.exists() || !dstFile.exists())
                                    continue;
                                
                                Run.initGenerators();
                                CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                jd.run();
                                
                                Set<ChangeData> insItems = jd.getInsertedItems();
                                for (ChangeData pair : insItems)
                                    insertedItems.add(pair);
                                Set<ChangeData> updItems = jd.getUpdatedItems();
                                for (ChangeData pair : updItems)
                                    updatedItems.add(pair);
                                Set<ChangeData> delItems = jd.getDeletedItems();
                                for (ChangeData pair : delItems)
                                    deletedItems.add(pair);
                            } 
                        }
                        //System.out.println(NLG.strInsertSentence(insertedItems));
                        //System.out.println(NLG.strUpdateSentence(updatedItems));
                        //System.out.println(NLG.strDeleteSentence(deletedItems));
                        
                        String result = NLG.generateChangeSentence(insertedItems, ChangeType.ADD);
                        if (!result.isEmpty()) bw.write(result + ". ");
                        result = NLG.generateChangeSentence(updatedItems, ChangeType.MODIFY);
                        if (!result.isEmpty()) bw.write(result + ". ");
                        result = NLG.generateChangeSentence(deletedItems, ChangeType.DELETE);
                        if (!result.isEmpty()) bw.write(result + ". ");
                        
                    } else {
                        bw.write("Discarded\n");
                    }
                    bw.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (fw != null)
                        fw.close();
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isDiscardCommit(List<DiffEntry> diffs) {
        
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
                
                if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
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
