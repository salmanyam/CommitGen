package se.commit.train;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
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
       
    public void generateDiffs(String filename) {
        ttry(Repository repository = JGitWrapper.openGitRepository(repoName)) {
            List<Commit> commitData = JGitWrapper.getAllCommits(repository);
            Collections.reverse(commitData);
            
            int total_files = (int)Math.ceil(commitData.size() / 1000.0);
            
            File[] file = new File[total_files];
            
            try {
                for (int i = 0; i < total_files; i++)
                    file[i] = new File(filename + "-diffs-" + i + ".txt");
                
                OutputStream os = new FileOutputStream(file[0]);
                
                try {
                    for (int revNo = 2; revNo < commitData.size(); revNo++) {
                        
                        if (revNo % 5000 == 0)
                            os.close();
                            os = new FileOutputStream(file[revNo / 5000]);
                        
                        JGitWrapper.getDiffBetweenCommits(os, 
                                repository, 
                                commitData.get(revNo-1).getRevision(), 
                                commitData.get(revNo).getRevision());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void generate(String filename) {
        try(Repository repository = JGitWrapper.openGitRepository(repoName)) {
            List<Commit> commitData = JGitWrapper.getAllCommits(repository);
            Collections.reverse(commitData);
            
            FileWriter fw = null;
            FileWriter timeFw = null;
            
            try {
                fw = new FileWriter(filename + ".txt");
                timeFw = new FileWriter(filename + "-timing.txt");
                try (BufferedWriter bw = new BufferedWriter(fw); BufferedWriter timeBw = new BufferedWriter(timeFw)) {
                    for (int revNo = 2; revNo < commitData.size(); revNo++) {
                        
                        List<DiffEntry> diffs = JGitWrapper.listDiff(
                                repository,
                                commitData.get(revNo-1).getRevision(),
                                commitData.get(revNo).getRevision());
                        
                        //bw.write("RevNo: " + revNo + ", Rev: " + commitData.get(revNo).getRevision() + ", Changes: " + diffs.size() + "\n");
                        //bw.write("Commit: " + commitData.get(revNo).getMessage() + "\n");
                        bw.write(revNo + "\t");
                        bw.write(commitData.get(revNo-1).getRevision() + "\t");
                        bw.write(commitData.get(revNo).getRevision() + "\t");
                        bw.write(commitData.get(revNo).getMessage() + "\t");
                        
                        if (!isDiscardCommit(diffs)) {
                            
                            long startTime = System.currentTimeMillis();
                            
                            Set<ChangeData> insertedItems = new HashSet<>();
                            Set<ChangeData> updatedItems = new HashSet<>();
                            Set<ChangeData> deletedItems = new HashSet<>();
                            
                            long timeForChangeExtraction = 0;
                            
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
                                    
                                    long gumStart = System.currentTimeMillis();
                                    
                                    Run.initGenerators();
                                    CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                                    jd.run();
                                    
                                    Set<ChangeData> insItems = jd.getInsertedItems();
                                    Set<ChangeData> updItems = jd.getUpdatedItems();
                                    Set<ChangeData> delItems = jd.getDeletedItems();
                                    
                                    long gumEnd = System.currentTimeMillis();
                                    
                                    timeForChangeExtraction += (gumEnd - gumStart);
                                    
                                    for (ChangeData pair : insItems)
                                        insertedItems.add(pair);
                                    for (ChangeData pair : updItems)
                                        updatedItems.add(pair);
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
                            String result1 = NLG.generateChangeSentence(insertedItems, ChangeType.ADD);
                            String result2 = NLG.generateChangeSentence(updatedItems, ChangeType.MODIFY);
                            String result3 = NLG.generateChangeSentence(deletedItems, ChangeType.DELETE);
                            
                            long endTime = System.currentTimeMillis();
                            
                            //Writing timing data to file
                            timeBw.write(commitData.get(revNo-1).getRevision() + "\t");
                            timeBw.write(commitData.get(revNo).getRevision() + "\t");
                            timeBw.write(""+timeForChangeExtraction + "\t");
                            timeBw.write(""+(endTime - startTime) + "\t");
                            timeBw.write("\n");
                            
                            if (!result1.isEmpty()) bw.write(result1 + " ");
                            if (!result2.isEmpty()) bw.write(result2 + " ");
                            if (!result3.isEmpty()) bw.write(result3 + " ");
                            
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
                    if (timeFw != null)
                        timeFw.close();
                    
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
