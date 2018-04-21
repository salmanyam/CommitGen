package se.commit.jgit;

import java.util.Date;

public class Commit {
    
    private String revision;
    private Date commitTime;
    private String message;
    
    public Commit(String revision, Date commitTime, String message) {
        this.revision = revision;
        this.commitTime = commitTime;
        this.message = message;
    }

    
    public String getRevision() {
        return revision;
    }

    
    public void setRevision(String revision) {
        this.revision = revision;
    }

    
    public Date getCommitTime() {
        return commitTime;
    }

    
    public void setCommitTime(Date commitTime) {
        this.commitTime = commitTime;
    }

    
    public String getMessage() {
        return message;
    }

    
    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
