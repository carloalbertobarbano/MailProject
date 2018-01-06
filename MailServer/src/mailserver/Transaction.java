/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author carloalberto
 */
public class Transaction {
    public static int uniqueId = 0;

    private int id; 
    public Transaction() {
        id = uniqueId++;
    }

    public int getUniqueId() {
        return id;
    }
    
    public void execute(int action, String path, mailclient.MailModel oldValue, mailclient.MailModel newValue) {
        TransactionAction ta = new TransactionAction(this, action, path, oldValue, newValue);
        TransactionManager.get().execute(ta);
    }
    
    public ArrayList<mailclient.MailModel> commit() throws IOException {
        return TransactionManager.get().commit(this);
    }
    
    public void abort() {
        TransactionManager.get().abort(this);
    }
    
}
