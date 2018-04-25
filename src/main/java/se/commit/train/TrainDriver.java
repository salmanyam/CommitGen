package se.commit.train;

import java.util.Random;

public class TrainDriver {

    public static void main(String[] args) {
        String repoName = "/Users/salman/eclipse-workspace/hadoop/.git";
        //String repoName = "/Users/salman/eclipse-workspace/slf4j/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/elasticsearch/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/spring-boot/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/spring-framework/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/TestProject/.git";
        
        GenerateTrain gt = new GenerateTrain(repoName);
        //gt.generate();
        Random rand = new Random();
        int revNo = rand.nextInt(16277) + 2;
        
        String filename = "data//hadoop.txt";

        gt.generate(filename);
    }
}
