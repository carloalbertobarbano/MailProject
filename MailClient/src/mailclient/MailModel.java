/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author carloalberto
 */
public class MailModel implements Serializable  {
    private String id;
    private String sender;
    private ArrayList<String> dest;
    private String subject;
    private String body;
    private String date;
    
    @Override
    public String toString() {
        return "From: " + sender + " \n" + 
               "To: " + dest.toString() + "\n" +
               "Subject: " + subject + "\n" + 
               "Date: " + date + "\n" +
               "-------------------------\n" +
               "ID: " + id + "\n" + 
               "-------------------------\n" +
                body;
                
    }
    
    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof MailModel) {
            return ((MailModel)o).getId().equals(this.getId());
        }
        
        return false;
    }
    
    public MailModel(String sender, ArrayList<String> dest, String subject, String body, String date, String id) {
        if (id == null)
            this.id = UUID.randomUUID().toString();
        else 
            this.id = id;
        
        this.sender = sender;
        this.dest = (ArrayList<String>)dest.clone();
        this.subject = subject;
        this.body = body;
        this.date = date;
    }
    
    //public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    public String getSender() { return sender; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public String getDate() { return date; }
    public ArrayList<String> getDest() { return dest; }
    
    
    public static final String SORT_DATE = "Date";
    public static final Comparator<MailModel> SortDate = new Comparator<MailModel>() {
        @Override
        public int compare(MailModel a, MailModel b) {
            return b.getDate().compareTo(a.getDate());
        }
    };
    
    public static final String SORT_SENDER = "Sender";
    public static final Comparator<MailModel> SortSender = new Comparator<MailModel>() {
        @Override
        public int compare(MailModel a, MailModel b) {
            return a.getSender().compareTo(b.getSender());
        }
    };
    
    public static final HashMap<String, Comparator<? super MailModel>> comparators = new HashMap<>();
    
    static {
        comparators.put(SORT_DATE, SortDate);
        comparators.put(SORT_SENDER, SortSender);
    }
}
