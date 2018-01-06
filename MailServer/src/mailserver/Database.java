/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.io.IOException;
import org.json.*;
import java.util.ArrayList;
import java.util.List;
import mailclient.MailModel;


/**
 *
 * @author carloalberto
 */
public class Database {
    private static Database instance = null;
    private final TransactionManager transactionManager;
    
    public static Database get() {
        if (instance == null)
            instance = new Database();
        
        return instance;
    }
    
    public Database() {
        transactionManager = TransactionManager.get();
    }
    
    public ArrayList<mailclient.MailModel> getAccountMailbox(String account, String mailbox) {
        Logger.log("Retrieving mailbox: " + String.format("/%s/%s", account, mailbox));
        
        ArrayList<MailModel> result = null;
        Transaction t = transactionManager.begin();
        try {
            
            t.execute(TransactionAction.READ, String.format("/%s/%s", account, mailbox), null, null);
            result = t.commit();
        
        } catch (IOException e) {
            Logger.error("Could not retrieve " + String.format("/%s/%s", account, mailbox) + ", aborting");
            t.abort();
            return null;
        }
        
        return result;
    }
    
    public boolean deleteMail(String account, String mailbox, MailModel mail) {
        Logger.log("Deleting mail " + mail.getId() + " from " + String.format("/%s/%s", account, mailbox));
        
        Transaction t = transactionManager.begin();

        try {
            t.execute(TransactionAction.DELETE, String.format("/%s/%s", account, mailbox), mail, null);
            t.commit();
        
        } catch (IOException e) {
            Logger.error("Could not delete mail " + mail.getId() + " from " + String.format("/%s/%s", account, mailbox) + ", aborting");
            Logger.error(e.getMessage());
            t.abort();
            return false;
        }
        
        return true;
    }
    
    public boolean insertMail(String account, String mailbox, MailModel mail) {
        Logger.log("Inserting mail " + mail.getId() + " in "  + String.format("/%s/%s", account, mailbox));
        
        Transaction t = transactionManager.begin();
        
        try {
            t.execute(TransactionAction.INSERT, String.format("/%s/%s", account, mailbox), null, mail);
            t.commit();
            
        } catch (IOException e) {
            Logger.error("Could not insert mail " + mail.getId() + " in "  + String.format("/%s/%s", account, mailbox) + ", aborting");
            Logger.error(e.getMessage());
            t.abort();
            return false;
        }
        
        return true;
    }
}
