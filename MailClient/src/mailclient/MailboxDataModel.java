/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Observable;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
/**
 *
 * @author carloalberto
 */
public class MailboxDataModel extends Observable implements IMailboxDataModel {
   
    private String account; 
    private ArrayList<ObservableList<MailModel>> mailbox;
    
    private static MailboxDataModel instance = null;
    
    private MailModel buildInboxMailFixture(int i) {
        ArrayList<String> dest = new ArrayList<>();
        dest.add("receiver1@email.com");
        return new MailModel("sender" + Integer.toString(i) + "@email.com", dest, "Test subject", "<strong>Lorem Ipsum</strong> is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", 
                             "2017-11-9 19:23:" + Integer.toString(i));
    }
    
    public MailboxDataModel() {
        mailbox = new ArrayList<>();
        
        for (int i = 0; i < Mailboxes.mailboxes_num; i++){
            mailbox.add(FXCollections.<MailModel>observableArrayList());
            
            for (int j = 0; j < 10; j++)
                mailbox.get(i).add(buildInboxMailFixture(j));
            
        }
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getAccount() { return account; }
       
    public ObservableList<MailModel> getMailbox(int mailbox) {
        return this.mailbox.get(mailbox);
    }
    
    public boolean deleteMail(int mailbox, MailModel mail) {
        this.mailbox.get(mailbox).remove(mail);
        return true;
    }
    
    public boolean sortMailbox(int mailbox, Comparator<? super MailModel> comparator) {
        this.mailbox.get(mailbox).sort(comparator);
        return true;
    }
    
    public boolean insertMail(int mailbox, MailModel mail) {
        this.mailbox.get(mailbox).add(mail);
        return true;
    }
    
    public boolean sendMail(MailModel mail) { return true; }
}
