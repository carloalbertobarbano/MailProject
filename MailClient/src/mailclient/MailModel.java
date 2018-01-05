/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author carloalberto
 */
public class MailModel implements Serializable  {
    private static int num = 0;
    
    private int id;
    private String sender;
    private ArrayList<String> dest;
    private String subject;
    private String body;
    private String date;
    
    public MailModel(String sender, ArrayList<String> dest, String subject, String body, String date) {
        this.id = num++;
        this.sender = sender;
        this.dest = (ArrayList<String>)dest.clone();
        this.subject = subject;
        this.body = body;
        this.date = date;
    }
    
    public int getId() { return id; }
    public String getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getDate() { return date; }
    public ArrayList<String> getDest() { return dest; }
    
    
    public static final String SORT_DATE = "Date";
    public static Comparator<MailModel> SortDate = new Comparator<MailModel>() {
        @Override
        public int compare(MailModel a, MailModel b) {
            return b.getDate().compareTo(a.getDate());
        }
    };
    
    public static final String SORT_SENDER = "Sender";
    public static Comparator<MailModel> SortSender = new Comparator<MailModel>() {
        @Override
        public int compare(MailModel a, MailModel b) {
            return a.getSender().compareTo(b.getSender());
        }
    };
}
