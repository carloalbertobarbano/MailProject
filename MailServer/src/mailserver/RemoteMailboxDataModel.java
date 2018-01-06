/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import mailclient.MailModel;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author carloalberto
 */
public class RemoteMailboxDataModel extends UnicastRemoteObject implements IRemoteMailboxDataModel {
    public static class Mailboxes {
        public static final int MAILBOX_INBOX = 0;
        public static final int MAILBOX_SENT = 1;
        public static final int MAILBOX_OUTBOX = 2;
        public static final int MAILBOX_SPAM = 3;
        public static final int  MAILBOX_TRASH = 4;
        
        public static final List<String> labels = Arrays.asList("Inbox", "Sent", "Outbox", "Spam", "Trash");
        public static final int mailboxes_num = 5;
    }
    
    private String account;
    private ArrayList< ArrayList<MailModel>> mailbox;
    private final Database database = Database.get();
    
    public RemoteMailboxDataModel() throws RemoteException {
        super();
        buildFixture();
    }
    
    private MailModel buildMailFixture(int i) {
        ArrayList<String> dest = new ArrayList<>();
        dest.add("receiver1@email.com");
        return new MailModel("sender" + Integer.toString(i) + "@email.com", dest, "Test subject", "<strong>Lorem Ipsum</strong> is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", 
                             "2017-11-9 19:23:" + Integer.toString(i));
    }
    
    private void buildFixture() {
        mailbox = new ArrayList<>();
        
        for (int i = 0; i < Mailboxes.mailboxes_num; i++) {
            mailbox.add(new ArrayList<>());
            
            for (int j = 0; j < 3; j++)
                mailbox.get(i).add(buildMailFixture(j));
            
        }
    }
    
    @Override
    public void setAccount(String account) throws RemoteException {
        System.out.println("REMOTE: setting account " + account);
        this.account = account;
    }
    
    @Override
    public String getAccount() throws RemoteException {
        return this.account;
    }
    
    @Override
    public ArrayList<MailModel> getMailbox(int mailbox) throws RemoteException {
         
        return this.database.getAccountMailbox(account, Mailboxes.labels.get(mailbox));
        
            //return this.mailbox.get(mailbox);
    }
    
    @Override
    public boolean deleteMail(int mailbox, MailModel mail) throws RemoteException {
        try {
            return this.mailbox.get(mailbox).remove(mail);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean insertMail(int mailbox, MailModel mail) throws RemoteException {
        try {
            System.out.println("REMOTE: inserting mail with id: " + mail.getId());
            return this.mailbox.get(mailbox).add(mail);
            
        } catch (Exception e) {
            return false;
        }
    }
}