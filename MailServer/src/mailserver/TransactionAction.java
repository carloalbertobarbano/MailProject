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
    public static final int BEGIN = 1;
    public static final int READ = 2;
    public static final int INSERT = 3;
    public static final int UPDATE = 4;
    public static final int DELETE = 5;
    public static final int END = 6;

    private final Transaction t;
    private final int action;
    private final String path;
    private final mailclient.MailModel data; 


    public TransactionAction(Transaction t, int action, String path, mailclient.MailModel data) {
        this.t = t; 
        this.action = action;
        this.path = path;
        this.data = data;
    }

    public Transaction getTransaction() { return t; }
    public int getAction() { return action; }
    public String getPath() { return path; }
    public mailclient.MailModel getData() { return data; }
}