/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.util.Comparator;
import javafx.collections.ObservableList;
import java.rmi.*;
/**
 *
 * @author carloalberto
 */
public class RemoteMailboxDataModel implements IMailboxDataModel {
    
    private IRemoteMailboxDataModel remoteMailboxDataModel;
    
    public RemoteMailboxDataModel() {
        try {
            System.setProperty("java.security.policy", "file:/home/carloalberto/Documents/Università/prog3/MailProject/MailClient/src/mailclient/client.policy");
            //System.out.println(MailServer.class.getResource("server.policy"));

            if (System.getSecurityManager() == null)
                System.setSecurityManager(new SecurityManager());
            remoteMailboxDataModel = (IRemoteMailboxDataModel)Naming.lookup("rmi://127.0.0.1:2000/mailserver");
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   
    
    public void setAccount(String account) {
        
    }
    
    public String getAccount() {
        try {
            return remoteMailboxDataModel.getAccount();
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public ObservableList<MailModel> getMailbox(int mailbox) {
        return null;
    }
    
    public boolean deleteMail(int mailbox, MailModel mail) {
        return false;
    }
    
    public boolean sortMailbox(int mailbox, Comparator<? super MailModel> comparator) {
        return false;
    }
    
    public boolean insertMail(int mailbox, MailModel mail) {
        return false;
    }
    
}
