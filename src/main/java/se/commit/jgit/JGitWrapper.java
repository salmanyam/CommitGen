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
/**
 * This is a static class. It contains all the necessary JGit wrapper functionalities to extract the changes of a revision.
 * @author salman
 *
 */
public class JGitWrapper {

	/**
	 * This method oepn the connectivity for a Git repository using the repository directory or path
	 * @param repoDir: path to a Git repository
	 * @return the instance of a Repository
	 */
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
    
    /**
     * This method gives the number of new files added to a Git repository from the head to the lastest stage of the repository.
     * @param repository: repository instance
     * @return a set of file paths
     */
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
    /**
     * This method gives the number of modified files added to a Git repository from the head to the lastest stage of the repository.
     * @param repository: repository instance
     * @return a set of file paths
     */
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
    
    /**
     * This method gives the number of deleted files added to a Git repository from the head to the lastest stage of the repository.
     * @param repository: repository instance
     * @return a set of file paths
     */
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
    
    /**
     * This method extract the file content of a file in the most recent commit
     * @param path: file path
     * @param repository: instance of a Repository
     * @return the file content as string
     */
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
    
    /**
     * This method extract the file content from a specified revision
     * @param path: file path
     * @param repository : instance of a Repository
     * @param revNo : revision number of the file
     * @return the file content as string
     */
    
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
    
    /**
     * This method extract all the commits or revisions from a git repository
     * @param repository : instance of a Repository
     * @return a list of all commits
     */
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
    
    /**
     * This method dumps the diff between two commit or revision IDs in a Git repository to a file
     * @param out : instance of an OutputDataStream of the file where the method dumps
     * @param repository : instance of the Repository
     * @param commitId1 : first commit ID
     * @param commitId2 : second commit ID
     * @param commitMsg : associated commit message for storing purpose
     */
    public static void getDiffBetweenCommits(OutputStream out, Repository repository, String commitId1, String commitId2, String commitMsg) {
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, commitId1);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, commitId2);
        String endMarker1 = "revi####commit##gen####vt####se###spring####2018\n";
        String endMarker2 = "comm####commit##gen####vt####se###spring####2018\n";
        String endMarker3 = "diff####commit##gen####vt####se###spring####2018\n";
        String revionsNo = commitId2 + "\n";
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
                out.write(endMarker1.getBytes());
                out.write(revionsNo.getBytes());
                out.write(endMarker2.getBytes());
                out.write(commitMsg.getBytes());
                out.write(endMarker3.getBytes());
                
                for (DiffEntry entry : diff) {
                    //System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                    try (DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(repository);
                        formatter.format(entry);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }  
    }
    
    /**
     * This is an associated utility method needed by the abovementioned methods
     * @param repository : instance of a repository
     * @param oldCommit : old commit 
     * @param newCommit : new commit
     * @return a list of diff entry
     */
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

    /**
     * This is also a utility method for the abovementioned methods
     * @param repository
     * @param objectId
     * @return abstract tree iterator
     */
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
