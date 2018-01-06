/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.util.Comparator;
import javafx.collections.ObservableList;
import java.rmi.*;
import java.util.ArrayList;
import javafx.collections.FXCollections;
/**
 *
 * @author carloalberto
 */
public class RemoteMailboxDataModel implements IMailboxDataModel {
    private ArrayList< ObservableList<MailModel>> mailbox;
    
    private mailserver.IRemoteMailboxDataModel remoteMailboxDataModel;
    
    public RemoteMailboxDataModel() {
        try {
            System.setProperty("java.security.policy", "file:/home/carloalberto/Documents/Università/prog3/MailProject/MailClient/src/mailclient/client.policy");

            if (System.getSecurityManager() == null)
                System.setSecurityManager(new SecurityManager());
            remoteMailboxDataModel = (mailserver.IRemoteMailboxDataModel)Naming.lookup("rmi://127.0.0.1:2000/mailserver");
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mailbox = new ArrayList<>();
        for (int i = 0; i < Mailboxes.mailboxes_num; i++)
            mailbox.add(FXCollections.<MailModel>observableArrayList());
    }
    
   
    
    public void setAccount(String account) {
        try {
            remoteMailboxDataModel.setAccount(account);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getAccount() {
        try {
            return remoteMailboxDataModel.getAccount();
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public ObservableList<mailclient.MailModel> getMailbox(int mailbox) {
        try {
            this.mailbox.get(mailbox).clear();
            ArrayList<mailclient.MailModel> remoteMailbox = remoteMailboxDataModel
                                                          .getMailbox(mailbox);
            this.mailbox.get(mailbox).addAll(remoteMailbox);
            return this.mailbox.get(mailbox);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean deleteMail(int mailbox, MailModel mail) {
        try {
            if(remoteMailboxDataModel.deleteMail(mailbox, mail)) {
                this.mailbox.get(mailbox).remove(mail);
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean sortMailbox(int mailbox, Comparator<? super MailModel> comparator) {
        try {
            this.mailbox.get(mailbox).sort(comparator);
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public boolean insertMail(int mailbox, MailModel mail) {
        try {
            return remoteMailboxDataModel.insertMail(mailbox, mail);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
}
