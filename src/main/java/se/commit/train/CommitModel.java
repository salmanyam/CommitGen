package se.commit.train;


public class CommitModel {
    
    private String generatedCommit;
    private String originalCommit;
    
    public CommitModel(String generatedCommit, String originalCommit) {

        this.generatedCommit = generatedCommit;
        this.originalCommit = originalCommit;
    }
    
    public String getGeneratedCommit() {
        return generatedCommit;
    }
    
    public void setGeneratedCommit(String generatedCommit) {
        this.generatedCommit = generatedCommit;
    }
    
    public String getOriginalCommit() {
        return originalCommit;
    }
    
    public void setOriginalCommit(String originalCommit) {
        this.originalCommit = originalCommit;
    }
    
    
}
