package se.commit.gen;


import java.io.BufferedWriter;

/** GIT sample code with BSD license. Copyright by Steve Jin */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.IndexDiff.StageState;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.client.diff.ClusterDiff;
import com.github.gumtreediff.client.diff.JsonDiff;
import com.github.gumtreediff.client.diff.TextDiff;
import com.github.gumtreediff.client.diff.CommitGen;
import com.github.gumtreediff.client.diff.swing.SwingDiff;
import com.github.gumtreediff.client.diff.web.DiffView;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.io.ActionsIoUtils;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.utils.Tuple;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.api.Status;

public class Driver
{
    public static void main(String[] args) throws IOException, GitAPIException {
        File gitWorkDir = new File("/Users/salman/eclipse-workspace/TestProject/.git");

        //Repository repository = openJGitCookbookRepository();
        try (Repository repository = openJGitCookbookRepository(gitWorkDir)) {
            Ref head = repository.findRef("refs/heads/master");
            System.out.println("Found head: " + head);

            // a RevWalk allows to walk over commits based on some filtering that is defined
            try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(head.getObjectId());

                System.out.println("\nCommit-Message: " + commit.getFullMessage());

                walk.dispose();
            }
        }
        
        // first create a test-repository, the return is including the .get directory here!
       // File gitWorkDir = new File("/Users/salman/eclipse-workspace/spring-framework/.git");

        String branch = "";
        String name = "";
        String message = "";
        //File gitWorkDir = new File(".");
        try {
            Git git = Git.open(gitWorkDir);
            Iterable<RevCommit> commits = git.log().all().call();
            Repository repo = git.getRepository();
            branch = repo.getBranch();
            RevCommit latestCommit = commits.iterator().next();
            name = latestCommit.getName();
            message = latestCommit.getFullMessage();
        } catch (Throwable e) {
            name = "";
            message = "";
            branch = "";
        }
        
        System.out.println(branch + " " + name + " " + message);
        
        System.out.println(getAllNewFile(gitWorkDir));
        System.out.println(getAllRemovedFile(gitWorkDir));
        System.out.println(getAllModifiedFile(gitWorkDir));    
        
        Set<String> modified = getAllModifiedFile(gitWorkDir);
        System.out.println(modified.size());
        
        
        Set<Tuple<String, String, String>> insertedItems = new HashSet<Tuple<String, String, String>>();
        Set<Tuple<String, String, String>> updatedItems = new HashSet<Tuple<String, String, String>>();
        Set<Tuple<String, String, String>> deletedItems = new HashSet<Tuple<String, String, String>>();
        
        try (Repository repository = openJGitCookbookRepository(gitWorkDir)) {
            for(String modify : modified) {
                System.out.println("Modification: " + modify);
                //System.out.println(getContentFromMostRecentCommit(modify, gitWorkDir));
                
                File modifiedFile = null;
                BufferedWriter writer = null;
                try{
                    
                    
                    modifiedFile = File.createTempFile("Temp"+ modify, ".java"); 
                    
                    writer = new BufferedWriter(new FileWriter(modifiedFile.getAbsoluteFile()));
                    writer.write(getContentFromMostRecentCommit(modify, gitWorkDir));
                    writer.close();
                    
                     
                 }catch(IOException e){

                    e.printStackTrace();
                     
                 }

                
                String fileName = modify;
                File tempFile = new File(repository.getDirectory().getParentFile(), fileName);
                
                Path tempFilePath = tempFile.toPath();
                
                //System.out.println(getTextFromFilePath(tempFilePath));
                String file1 = modifiedFile.getAbsolutePath();
                String file2 = tempFile.getAbsolutePath();
                
                
                Run.initGenerators();
                //ITree src = Generators.getInstance().getTree(file1).getRoot();
                //ITree dst = Generators.getInstance().getTree(file2).getRoot();
                //Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
                //m.match();
                //ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
                //g.generate();
                //List<Action> actions = g.getActions(); // return the actions
                
                //for (Action action : actions) {
                  //  System.out.println(action.getName() + " " + action.getNode().getId() + " " + action.getNode().getLabel());
                //}
                
                
                System.out.println("Diff start here....");
                CommitGen jd = new CommitGen(new String[] {file1, file2});
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
                
                //break;
                
            }
            
            for (Tuple<String, String, String> pair : insertedItems) {
                System.out.println(pair.toString());
            }
            System.out.println();
            for (Tuple<String, String, String> pair : updatedItems) {
                System.out.println(pair.toString());
            }
            System.out.println();
            for (Tuple<String, String, String> pair : deletedItems) {
                System.out.println(pair.toString());
            }
            System.out.println();
            System.out.print(generateInsertSentence(insertedItems));
            System.out.println(generateUpdateSentence(updatedItems));
            
        }
    }
   
    
    public static String generateInsertSentence(Set<Tuple<String, String, String>> insertItems) {
        String sentences = "";
        Map<String, Set<String>> propertyMaps = new HashMap<String, Set<String>>();
        Set<String> classes = new HashSet<String>();
        for (Tuple<String, String, String> tuple : insertItems) {
            Set<String> hs = propertyMaps.get(tuple.third);
            if (hs == null) {
                hs = new HashSet<String>();
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
        Map<String, Set<String>> propertyMaps = new HashMap<String, Set<String>>();
        
        for (Tuple<String, String, String> tuple : updateItems) {
            Set<String> hs = propertyMaps.get(tuple.third);
            if (hs == null) {
                hs = new HashSet<String>();
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
    
    
    public static Set<String> getAllNewFile(File gitWorkDir) {        
        try (Repository repository = openJGitCookbookRepository(gitWorkDir)) {
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
    
    public static Set<String> getAllRemovedFile(File gitWorkDir) {        
        try (Repository repository = openJGitCookbookRepository(gitWorkDir)) {
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
    
    public static Set<String> getAllModifiedFile(File gitWorkDir) {        
        try (Repository repository = openJGitCookbookRepository(gitWorkDir)) {
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
    
    public static Repository openJGitCookbookRepository(File repoDir) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .setGitDir(repoDir)
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
    }
    
    
    
    
    private static void listDiff(Repository repository, Git git, String oldCommit, String newCommit) throws GitAPIException, IOException {
        final List<DiffEntry> diffs = git.diff()
                .setOldTree(prepareTreeParser(repository, oldCommit))
                .setNewTree(prepareTreeParser(repository, newCommit))
                .call();

        System.out.println("Found: " + diffs.size() + " differences");
        for (DiffEntry diff : diffs) {
            System.out.println("Diff: " + diff.getChangeType() + ": " +
                    (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()));
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
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
    }
    
    public static String getContentFromMostRecentCommit(String path, File gitWorkDir) throws IOException {
        
        try (Repository repository = openJGitCookbookRepository(gitWorkDir)) {
            Ref head = repository.findRef("refs/heads/master");
            //System.out.println("Found head: " + head);

            // a RevWalk allows to walk over commits based on some filtering that is defined
            try (RevWalk walk = new RevWalk(repository)) {
                RevCommit commit = walk.parseCommit(head.getObjectId());

                //System.out.println("\nCommit-Message: " + commit.getFullMessage());
                
                
                try (TreeWalk treeWalk = TreeWalk.forPath(repository, path, commit.getTree())) {
                    ObjectId blobId = treeWalk.getObjectId(0);
                    try (ObjectReader objectReader = repository.newObjectReader()) {
                      ObjectLoader objectLoader = objectReader.open(blobId);
                      byte[] bytes = objectLoader.getBytes();
                      return new String(bytes, StandardCharsets.UTF_8);
                    }
                  }
                

                //walk.dispose();
            }
            
            
        }        
      }
    

    
   /* 
    public revertFileToMostRecentCommit() {
        
        try (Repository repository = createNewRepository()) {
            System.out.println("Listing local branches:");
            try (Git git = new Git(repository)) {
                // set up a file
                String fileName = "temptFile.txt";
                File tempFile = new File(repository.getDirectory().getParentFile(), fileName);
                if(!tempFile.createNewFile()) {
                    throw new IOException("Could not create tempfile " + tempFile);
                }
                Path tempFilePath = tempFile.toPath();

                // write some initial text to it
                String initialText = "Initial Text";
                System.out.println("Writing text [" + initialText + "] to file [" + tempFile.toString() + "]");
                Files.write(tempFilePath, initialText.getBytes());

                // add the file and commit it
                git.add().addFilepattern(fileName).call();
                git.commit().setMessage("Added untracked file " + fileName + "to repo").call();

                // modify the file
                Files.write(tempFilePath, "Some modifications".getBytes(), StandardOpenOption.APPEND);

                // assert that file's text does not equal initialText
                if (initialText.equals(getTextFromFilePath(tempFilePath))) {
                    throw new IllegalStateException("Modified file's text should not equal " +
                            "its original state after modification");
                }

                System.out.println("File now has text [" + getTextFromFilePath(tempFilePath) + "]");

                // revert the changes
                git.checkout().addPath(fileName).call();

                // text should no longer have modifications
                if (!initialText.equals(getTextFromFilePath(tempFilePath))) {
                    throw new IllegalStateException("Reverted file's text should equal its initial text");
                }

                System.out.println("File modifications were reverted. " +
                        "File now has text [" + getTextFromFilePath(tempFilePath) + "]");
            }
        }
    }
    */
    public static String getTextFromFilePath(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        CharBuffer chars = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        return chars.toString();
    }
}
