package se.commit.jgit;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import javafx.util.Pair;

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
    
    public static String getFileContentFromRevision(String path, Repository repository, String revNo) {
        
        try {    
                
            try (RevWalk walk = new RevWalk(repository)) {
                ObjectId commitId = repository.resolve(revNo);
                RevCommit commit = walk.parseCommit(commitId);
               
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
    
    public static List<Commit> getAllCommits(Repository repository){
        
        List<Commit> result = new Vector<>();

        try {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> logs = git.log()
                        .call();
                 
                for (RevCommit rev : logs) {
                    String msg = rev.getShortMessage().replaceAll("[\\t\\n\\r]+"," ");
                    result.add(new Commit(rev.getName(), rev.getAuthorIdent().getWhen(), msg));
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return result;
    }
    
    public static void getDiffBetweenCommits(OutputStream out, Repository repository, String commitId1, String commitId2) {
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, commitId1);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, commitId2);
        String endMarker = "####commit##gen####vt####se###spring####2018\n";
        
        try {
            // then the porcelain diff-command returns a list of diff entries
            try (Git git = new Git(repository)) {
                List<DiffEntry> diff = git.diff().
                        setOldTree(oldTreeParser).
                        setNewTree(newTreeParser).
                        //setPathFilter(PathFilter.create("README.md")).
                        // to filter on Suffix use the following instead
                        //setPathFilter(PathSuffixFilter.create(".java")).
                        call();
                for (DiffEntry entry : diff) {
                    //System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                    try (DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(repository);
                        formatter.format(entry);
                        out.write(endMarker.getBytes());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }  
    }
    
    public static List<DiffEntry> listDiff(Repository repository, String oldCommit, String newCommit) {
        try {
            try (Git git = new Git(repository)) {
                List<DiffEntry> diffs = git.diff()
                        .setOldTree(prepareTreeParser(repository, oldCommit))
                        .setNewTree(prepareTreeParser(repository, newCommit))
                        .call();
                
                return diffs;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) {
        
        try {
            try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(repository.resolve(objectId));
                RevTree tree = walk.parseTree(commit.getTree().getId());
    
                CanonicalTreeParser treeParser = new CanonicalTreeParser();
                try (ObjectReader reader = repository.newObjectReader()) {
                    treeParser.reset(reader, tree.getId());
                }
    
                walk.dispose();
    
                return treeParser;
            }
        } catch (IOException ex) {
            
            ex.printStackTrace();
        }
        
        return null;       
    }
}
