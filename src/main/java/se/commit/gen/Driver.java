package se.commit.gen;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class Driver
{
    public static void main(String[] args) {
        
        String repoName = "/Users/salman/eclipse-workspace/TestProject/.git"; 
        
        CommitGenerator cg = new CommitGenerator(repoName);
        cg.generate();
        
    }
   

    public static String getTextFromFilePath(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        CharBuffer chars = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        return chars.toString();
    }
}
