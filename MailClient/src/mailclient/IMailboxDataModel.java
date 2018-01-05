/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javafx.collections.ObservableList;

/**
 *
 * @author carloalberto
 */
public interface IMailboxDataModel {
    public static class Mailboxes {
        public static final int MAILBOX_INBOX = 0;
        public static final int MAILBOX_SENT = 1;
        public static final int MAILBOX_OUTBOX = 2;
        public static final int MAILBOX_SPAM = 3;
        public static final int  MAILBOX_TRASH = 4;
        
        public static final List<String> labels = Arrays.asList("Inbox", "Sent", "Outbox", "Spam", "Trash");
        public static final int mailboxes_num = 5;
    }
    
    //public static IMailboxDataModel get();
    
    public void setAccount(String account);
    
    public String getAccount();
    
    public ObservableList<MailModel> getMailbox(int mailbox);
    
    public boolean deleteMail(int mailbox, MailModel mail);
    
    public boolean sortMailbox(int mailbox, Comparator<? super MailModel> comparator);
    
    public boolean insertMail(int mailbox, MailModel mail);
            
    
}
