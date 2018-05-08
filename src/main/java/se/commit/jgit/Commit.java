package se.commit.jgit;

import java.util.Date;

/**
 * @author salman
 *	This class contains the basic information of a revision or commit
 */
public class Commit {
    
    private String revision;
    private Date commitTime;
    private String message;
    
    /**
     * Constructor for the Commit class
     * @param revision: commit id or the number of a diff
     * @param commitTime: the time when the commit is performed
     * @param message: the message used for committing a diff
     */
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
