/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

/**
 *
 * @author carloalberto
 */
public class TransactionAction {
    public static final int BEGIN = 0;
    public static final int READ = 1;
    public static final int INSERT = 2;
    public static final int UPDATE = 3;
    public static final int DELETE = 4;
    public static final int END = 5;

    private final Transaction t;
    private final int action;
    private final String path;
    private final mailclient.MailModel oldValue;
    private final mailclient.MailModel newValue; 


    public TransactionAction(Transaction t, int action, String path, 
                             mailclient.MailModel oldValue, mailclient.MailModel newValue) {
        this.t = t; 
        this.action = action;
        this.path = path;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Transaction getTransaction() { return t; }
    public int getAction() { return action; }
    public String getPath() { return path; }
    public mailclient.MailModel getOldValue() { return oldValue; }
    public mailclient.MailModel getNewValue() { return newValue; }
}