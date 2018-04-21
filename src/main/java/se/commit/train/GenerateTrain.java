package se.commit.train;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;

import com.google.common.io.Files;

import se.commit.jgit.Commit;
import se.commit.jgit.JGitWrapper;
import se.commit.utils.Utils;

public class GenerateTrain {
    
    public static void main(String[] args) {
        
        String repoName = "/Users/salman/eclipse-workspace/spring-framework/.git"; 
        int revNo = 100;
        
        try(Repository repository = JGitWrapper.openGitRepository(repoName)){
            
            List<Commit> commitData = JGitWrapper.getAllCommits(repository);
            Collections.reverse(commitData);
            
            System.out.println(commitData.size());
            showAvgFileInvovledInCommit(commitData, repository);
            
            List<DiffEntry> diffs = JGitWrapper.listDiff(
                    repository,
                    commitData.get(revNo-1).getRevision(),
                    commitData.get(revNo).getRevision());
                
            String results = commitData.get(revNo).getMessage();
            results += "=====================================================\n";
            results += "Found: " + diffs.size() + " differences\n";

            for (DiffEntry diff : diffs) {
                results += "Diff: " + diff.getChangeType() + ": " +
                        (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()) + "\n";
            }
            System.out.println(results);
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
    
    public static void showAvgFileInvovledInCommit(List<Commit> commitData, Repository repository) {
    
        int revNo = 2;
        int total = 0;
        long changes = 0;
        
        String filename = "data//change_data.txt";
        
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {

            fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);
            
            for (revNo = 2; revNo < commitData.size(); revNo++) {
                List<DiffEntry> diffs = JGitWrapper.listDiff(
                        repository,
                        commitData.get(revNo-1).getRevision(),
                        commitData.get(revNo).getRevision());
                
                if (isDiscardCommit(diffs)) continue;
                
                bw.write(Utils.toDate(commitData.get(revNo).getCommitTime()) + " " + diffs.size() + "\n");
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
    }
}
