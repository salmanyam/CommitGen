package se.commit.gen;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.lib.Repository;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.client.diff.ChangeData;
import com.github.gumtreediff.client.diff.CommitGen;
import com.google.common.io.Files;

import se.commit.jgit.Commit;
import se.commit.jgit.JGitWrapper;
import se.commit.utils.Utils;

public class ChangeGui {
   private JFrame mainFrame;
   private JPanel controlPanel;
   private JButton nextButton;
   private JTextArea textArea;
   private JScrollPane sbrText;
   
   Repository repository;
   private int revNo;
   List<Commit> commitData;

   public ChangeGui(Repository repository, List<Commit> cdata, int revNo){
      
       this.repository = repository;
       this.commitData = cdata;
       this.revNo = revNo;
       
       prepareGui();
       
       
   }
   
   private void prepareGui() {
       mainFrame = new JFrame("Commit change log");
       mainFrame.setSize(1200, 700);
       //mainFrame.setLayout(new GridLayout(1, 1));
       
       
       
       controlPanel = new JPanel();
       controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
       controlPanel.setSize(1200, 100);
       
       nextButton = new JButton("Next");       
       nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);

       textArea = new JTextArea();
       textArea.setSize(1200, 600);
       
       sbrText = new JScrollPane(textArea);
       sbrText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
       //controlPanel.add(sbrText);
       
   }
   
   public void displayGui() {
       mainFrame.addWindowListener(new WindowAdapter() {
           @Override
           public void windowClosing(WindowEvent windowEvent){
               System.exit(0);
           }        
       });      
       
       nextButton.setActionCommand("Next");
       nextButton.addActionListener(new ButtonClickListener());
       
       controlPanel.add(nextButton);
       controlPanel.add(sbrText);
       
       mainFrame.add(controlPanel);
       mainFrame.setVisible(true);
   }

   private class ButtonClickListener implements ActionListener{
       @Override
       public void actionPerformed(ActionEvent e) {
           String command = e.getActionCommand();  
           
           Random rand = new Random();
           revNo = rand.nextInt(16277) + 2;
         
           if( command.equals( "Next" ))  {
               List<DiffEntry> diffs = JGitWrapper.listDiff(
                       repository,
                       commitData.get(revNo-1).getRevision(),
                       commitData.get(revNo).getRevision());
               
               String results = revNo + ": " + commitData.get(revNo-1).getRevision() + " " + commitData.get(revNo).getRevision() + "\n";
               results += commitData.get(revNo).getMessage() + "\n";
               
               if (isDiscardCommit(diffs)) {
                   results += "DISCARDED\n=================\n";
               }
               
               results += "=====================================================\n";
               results += "Found: " + diffs.size() + " differences\n";

               for (DiffEntry diff : diffs) {
                   results += "Diff: " + diff.getChangeType() + ": " +
                           (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath() : diff.getOldPath() + " -> " + diff.getNewPath()) + "\n";
               }
               results += "\n====================================================================\n";
               
               if (!isDiscardCommit(diffs)) {
                   Set<ChangeData> insertedItems = new HashSet<>();
                   Set<ChangeData> updatedItems = new HashSet<>();
                   Set<ChangeData> deletedItems = new HashSet<>();
                   
                   for (DiffEntry diff : diffs) {
                       
                       if (diff.getChangeType() == ChangeType.ADD) {
                           
                           if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                               continue;
                           
                           File srcFile = Utils.createTempFile("dummy", "");
                           File dstFile = Utils.createTempFile(
                                   diff.getNewPath(), 
                                   JGitWrapper.getFileContentFromRevision(
                                           diff.getNewPath(), 
                                           repository, 
                                           commitData.get(revNo).getRevision()));
                           
                           
                           if (!srcFile.exists() || !dstFile.exists())
                               continue;
                           
                           //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                           
                           Run.initGenerators();
                           CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                           jd.run();
                           
                           Set<ChangeData> insItems = jd.getInsertedItems();
                           for (ChangeData pair : insItems)
                               insertedItems.add(pair);
                           Set<ChangeData> updItems = jd.getUpdatedItems();
                           for (ChangeData pair : updItems)
                               updatedItems.add(pair);
                           Set<ChangeData> delItems = jd.getDeletedItems();
                           for (ChangeData pair : delItems)
                               deletedItems.add(pair);
                           
                           
                           
                       } else if (diff.getChangeType() == ChangeType.DELETE) {
                           
                           if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                               continue;
                           
                           File srcFile = Utils.createTempFile(
                                   "dummy", 
                                   JGitWrapper.getFileContentFromRevision(
                                           diff.getOldPath(), 
                                           repository, 
                                           commitData.get(revNo-1).getRevision()));
                           
                           File dstFile = Utils.createTempFile("dummy", "");
                           
                           
                           if (!srcFile.exists() || !dstFile.exists())
                               continue;
                           
                           //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                           
                           
                           Run.initGenerators();
                           CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                           jd.run();
                           
                           Set<ChangeData> insItems = jd.getInsertedItems();
                           for (ChangeData pair : insItems)
                               insertedItems.add(pair);
                           Set<ChangeData> updItems = jd.getUpdatedItems();
                           for (ChangeData pair : updItems)
                               updatedItems.add(pair);
                           Set<ChangeData> delItems = jd.getDeletedItems();
                           for (ChangeData pair : delItems)
                               deletedItems.add(pair);
                           
                       } else if (diff.getChangeType() == ChangeType.MODIFY) {
                           
                           if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                               continue;
                           
                           File srcFile = Utils.createTempFile(
                                   "dummy", 
                                   JGitWrapper.getFileContentFromRevision(
                                           diff.getNewPath(), 
                                           repository, 
                                           commitData.get(revNo-1).getRevision()));
                           
                           File dstFile = Utils.createTempFile(
                                   "dummy", 
                                   JGitWrapper.getFileContentFromRevision(
                                           diff.getNewPath(), 
                                           repository, 
                                           commitData.get(revNo).getRevision()));
                           
                           //System.out.println(srcFile.getAbsolutePath() + " " + dstFile.getAbsolutePath());
                           
                           if (!srcFile.exists() || !dstFile.exists())
                               continue;
                           
                           
                           
                           Run.initGenerators();
                           CommitGen jd = new CommitGen(new String[] {srcFile.getAbsolutePath(), dstFile.getAbsolutePath()});
                           jd.run();
                           
                           Set<ChangeData> insItems = jd.getInsertedItems();
                           for (ChangeData pair : insItems)
                               insertedItems.add(pair);
                           Set<ChangeData> updItems = jd.getUpdatedItems();
                           for (ChangeData pair : updItems)
                               updatedItems.add(pair);
                           Set<ChangeData> delItems = jd.getDeletedItems();
                           for (ChangeData pair : delItems)
                               deletedItems.add(pair);
                       } 
                   }
                   
                   results += CommitGenerator.generateInsertSentence(insertedItems) + "\n";
                   results += CommitGenerator.generateUpdateSentence(updatedItems) + "\n";
                   results += CommitGenerator.generateDeleteSentence(deletedItems) + "\n";
               }
               
               
               textArea.setText(results);
           }    
       }     
   }
   
   public static boolean isDiscardCommit(List<DiffEntry> diffs) {
       
       int added = 0, modified = 0, deleted = 0;
       int total = 0;
       
       if (diffs.size() <= 0)
           return true;
       
       for (DiffEntry diff : diffs) {
                       
           if (diff.getChangeType() == ChangeType.ADD) {
               
               if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                   continue;
               
               added++;
               
           } else if (diff.getChangeType() == ChangeType.MODIFY) {
               
               if (!Files.getFileExtension(diff.getNewPath()).equals("java"))
                   continue;
               
               modified++;
               
           } else if (diff.getChangeType() == ChangeType.DELETE) {
               
               if (!Files.getFileExtension(diff.getOldPath()).equals("java"))
                   continue;
               
               deleted++;
               
           } else {
               continue;
           }           
       }
       
       total = added + modified + deleted;
       
       if (total <= 0) return true;
       if (modified > 10) return true;
       if (total > 15) return true;
               
       return false;
   }
}
