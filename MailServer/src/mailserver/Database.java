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
            
            t.execute(TransactionAction.READ, String.format("/%s/%s", account, mailbox), null);
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
            t.execute(TransactionAction.DELETE, String.format("/%s/%s", account, mailbox), mail);
            t.commit();
        
        } catch (IOException e) {
            Logger.error("Could not delete mail " + mail.getId() + " from " + String.format("/%s/%s", account, mailbox) + ", aborting");
            return false;
        }
        
        return true;
    }
}
