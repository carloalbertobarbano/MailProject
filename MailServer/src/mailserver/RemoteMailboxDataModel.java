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
    private final Database database = Database.get();
    
    public RemoteMailboxDataModel() throws RemoteException {
        super();
    }
    
    @Override
    public ArrayList<MailModel> getMailbox(String account, int mailbox) throws RemoteException, AccountNotFoundException {
        return this.database.getAccountMailbox(account, Mailboxes.labels.get(mailbox));
    }
    
    @Override
    public boolean deleteMail(String account, int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException {
        return this.database.deleteMail(account, Mailboxes.labels.get(mailbox), mail);
    }
    
    @Override
    public boolean insertMail(String account, int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException {
        return this.database.insertMail(account, Mailboxes.labels.get(mailbox), mail);
           
    }
    
    @Override
    public boolean sendMail(String account, MailModel mail) throws RemoteException, AccountNotFoundException {
        return this.database.sendMail(account, mail);
    }
}