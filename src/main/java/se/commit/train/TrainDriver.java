package se.commit.train;

import java.util.Random;

/**
 * This is the driver class for generating datasets
 * @author salman
 *
 */
public class TrainDriver {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("java -jar CommitGen.jar <repoName> <output filename>");
            System.exit(0);
        }
        //String repoName = "/Users/salman/eclipse-workspace/hadoop/.git";
        //String repoName = "/Users/salman/eclipse-workspace/slf4j/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/elasticsearch/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/spring-boot/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/spring-framework/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/TestProject/.git";
        
        String repoName = args[0];
        
        GenerateTrain gt = new GenerateTrain(repoName);
        //gt.generate();
        Random rand = new Random();
        int revNo = rand.nextInt(16277) + 2;
        
        String filename = args[1];
        //String filename = "data//sprint-framework.txt";

        gt.generateDiffs(filename);
    }
}
