package se.commit.train;

import se.commit.nlg.ChangeTranslator;

public class TrainDriver {

    public static void main(String[] args) {
        String repoName = "/Users/salman/eclipse-workspace/spring-framework/.git"; 
        //String repoName = "/Users/salman/eclipse-workspace/TestProject/.git";
        
        GenerateTrain gt = new GenerateTrain(repoName);
        gt.generate();
    }
}
