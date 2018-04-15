package se.commit.gen;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

public class JGitWrapper {

    public static Repository openGitRepository(String repoDir) {
 
        try {
            File gitWorkDir = new File(repoDir);
            
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            return builder
                    .setGitDir(gitWorkDir)
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public static Set<String> getNewFiles(Repository repository) {        
        try {
            try (Git git = new Git(repository)) {
                
                Status status = git.status().call();
                return status.getAdded();
               
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static Set<String> getModifiedFiles(Repository repository) {      
                
        try {
            try (Git git = new Git(repository)) {
                
                Status status = git.status().call();
                return status.getModified();
               
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            
            return null;
        }
    }
    
    public static Set<String> getAllRemovedFile(Repository repository) {        
        try {
            try (Git git = new Git(repository)) {
                
                Status status = git.status().call();
                return status.getRemoved();
               
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
            return null;
        }
    }
    
    public static String getFileContentFromRecentCommit(String path, Repository repository) {
        
        try {    
            
            Ref head = repository.findRef("refs/heads/master");
                
            try (RevWalk walk = new RevWalk(repository)) {
                
                RevCommit commit = walk.parseCommit(head.getObjectId());
               
                try (TreeWalk treeWalk = TreeWalk.forPath(repository, path, commit.getTree())) {
                    
                    ObjectId blobId = treeWalk.getObjectId(0);
                    
                    try (ObjectReader objectReader = repository.newObjectReader()) {
                        
                        ObjectLoader objectLoader = objectReader.open(blobId);
                        byte[] bytes = objectLoader.getBytes();
                        
                        return new String(bytes, StandardCharsets.UTF_8);
                    }                    
                }
            }
            
        } catch (Exception ex) {
            
            ex.printStackTrace();
            
            return null;
        }
    }
}
