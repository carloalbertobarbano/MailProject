/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import org.json.*;
import java.util.ArrayList;
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
        TransactionManager.Transaction t = transactionManager.begin();
        transactionManager.execute(new TransactionManager.TransactionAction(
                                        t,
                                        TransactionManager.TransactionAction.READ, 
                                        "/" + account + "/" + mailbox, null));
        JSONObject json = transactionManager.commit(t);
        
        ArrayList<mailclient.MailModel> result = new ArrayList<>();
        
        for (int i = 0; i < json.getJSONArray(mailbox).length(); i++) {
            JSONObject mail = json.getJSONArray(mailbox).getJSONObject(i);
            
            String sender = mail.getString("sender");
            ArrayList<String> dest = new ArrayList<>();
            JSONArray jsonArrayDest = mail.getJSONArray("dest");
            for (int j = 0; j < jsonArrayDest.length(); j++)
                dest.add(jsonArrayDest.getString(i));
            
            String subject = mail.getString("subject");
            String body = mail.getString("body");
            String date = mail.getString("date");
            
            result.add(new MailModel(sender, dest, subject, body, date));
        }
        
        return result;
    }
}
