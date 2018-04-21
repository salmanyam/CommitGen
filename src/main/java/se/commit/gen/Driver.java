package se.commit.gen;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

import com.github.gumtreediff.client.diff.swing.SwingDiff;

import javafx.util.Pair;
import se.commit.jgit.Commit;
import se.commit.jgit.JGitWrapper;

public class Driver
{
    public static void main(String[] args) {
        
        String repoName = "/Users/salman/eclipse-workspace/spring-framework/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/TestProject/.git";
        List<Commit> commitData = null;
        
        try(Repository repository = JGitWrapper.openGitRepository(repoName)){
            commitData = JGitWrapper.getAllCommits(repository);
            Collections.reverse(commitData);
            
            //System.out.println(commitData.size());
            
            //ChangeGui swingControlDemo = new ChangeGui(repository, commitData, 14695);  
            //swingControlDemo.displayGui();
        }
        
        CommitGenerator cg = new CommitGenerator(repoName);
        //cg.generate();
        cg.generateGUI(commitData, 14695);
        
        
    }
   

    public static String getTextFromFilePath(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        CharBuffer chars = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        return chars.toString();
    }
}
