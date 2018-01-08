/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import org.json.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mailclient.MailModel;


/**
 *
 * @author carloalberto
 */
public class Database {
    private static Database instance = null;
    private final TransactionManager transactionManager;
    
    public static final String SYSTEM_ACCOUNT = "noreply@mailserver";
    
    public static Database get() {
        if (instance == null)
            instance = new Database();
        
        return instance;
    }
    
    public Database() {
        transactionManager = TransactionManager.get();
    }
    
    public ArrayList<mailclient.MailModel> getAccountMailbox(String account, String mailbox) throws RemoteException, AccountNotFoundException {
        //Logger.log("Retrieving mailbox: " + String.format("/%s/%s", account, mailbox));
        
        ArrayList<MailModel> result = null;
        Transaction t = transactionManager.begin();
        try {
            
            t.execute(TransactionAction.READ, String.format("/%s/%s", account, mailbox), null, null);
            result = t.commit();
        
        } catch (IOException e) {
            Logger.error("Could not retrieve " + String.format("/%s/%s", account, mailbox) + ", aborting");
            t.abort();
            
            throw new RemoteException("Could not retrieve mailbox, aborting");
        }
        
        return result;
    }
    
    public boolean deleteMail(String account, String mailbox, MailModel mail) throws RemoteException, AccountNotFoundException {
        Logger.log("Deleting mail " + mail.getId() + " from " + String.format("/%s/%s", account, mailbox));
        
        Transaction t = transactionManager.begin();

        try {
            t.execute(TransactionAction.DELETE, String.format("/%s/%s", account, mailbox), mail, null);
            t.commit();
        
        } catch (IOException e) {
            Logger.error("Could not delete mail " + mail.getId() + " from " + String.format("/%s/%s", account, mailbox) + ", aborting");
            Logger.error(e.getMessage());
            t.abort();
            
            throw new RemoteException("Could not dellete mail, aborting");
        }
        
        return true;
    }
    
    public boolean insertMail(String account, String mailbox, MailModel mail) throws RemoteException, AccountNotFoundException {
        Logger.log("Inserting mail " + mail.getId() + " in "  + String.format("/%s/%s", account, mailbox));
        
        Transaction t = transactionManager.begin();
        
        try {
            t.execute(TransactionAction.INSERT, String.format("/%s/%s", account, mailbox), null, mail);
            t.commit();
            
        } catch (AccountNotFoundException e ) { 
            t.abort();
            throw new AccountNotFoundException(e.getMessage());
            
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("Could not insert mail " + mail.getId() + " in "  + String.format("/%s/%s", account, mailbox) + ", aborting");
            Logger.error(e.getMessage());
            t.abort();
            
            throw new RemoteException("Could not insert mail, aborting");
        }
        
        return true;
    }
    
    public boolean sendMail(String account, MailModel mail) throws RemoteException, AccountNotFoundException {
        Logger.log("Sending mail from " + account + " to " + mail.getDest());
        
        
        try {
            this.insertMail(account, Mailboxes.labels.get(Mailboxes.MAILBOX_SENT), mail);
            
            ArrayList<String> missingAccounts = new ArrayList<>();
            
            for (String dest : mail.getDest()) {
                try {
                    this.insertMail(dest, Mailboxes.labels.get(Mailboxes.MAILBOX_INBOX), mail);
                
                } catch (AccountNotFoundException e) {
                    missingAccounts.add(dest);
                }
            }
            
            if (missingAccounts.size() > 0) {
                ArrayList<String> dest = new ArrayList<>();
                dest.add(account);
                
                
                MailModel deliveryStatusMail = new MailModel(SYSTEM_ACCOUNT, dest,
                                                             "Delivery Status notification: failure", 
                                                             "Your email could not be delivered to the following containers: " + missingAccounts +
                                                             "\n\n\n" + "----Original email body------\n" + 
                                                             mail.toString(),
                                                             new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()), null);
                this.insertMail(account, Mailboxes.labels.get(Mailboxes.MAILBOX_INBOX), deliveryStatusMail);
            }
            
            
            
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("Could not send mail from " + account + " to " + mail.getDest());
            Logger.error(e.getMessage());
            
            throw new RemoteException("Could not send mail!");
        }
        
        return true;
    }
   
}
