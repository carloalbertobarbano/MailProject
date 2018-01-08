/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.net.MalformedURLException;
import java.util.Comparator;
import javafx.collections.ObservableList;
import java.rmi.*;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import mailserver.AccountNotFoundException;
import mailserver.Mailboxes;
/**
 *
 * @author carloalberto
 */
public class RemoteMailboxDataModel implements IMailboxDataModel {
    private ArrayList< ObservableList<MailModel>> mailbox;
    
    private mailserver.IRemoteMailboxDataModel remoteMailboxDataModel;
    
    public RemoteMailboxDataModel() throws NotBoundException, MalformedURLException, RemoteException {
        
        System.setProperty("java.security.policy", "file:/home/carloalberto/Documents/Università/prog3/MailProject/MailClient/src/mailclient/client.policy");

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        remoteMailboxDataModel = (mailserver.IRemoteMailboxDataModel)Naming.lookup("rmi://127.0.0.1:2000/mailserver");
        
        
        mailbox = new ArrayList<>();
        for (int i = 0; i < Mailboxes.mailboxes_num; i++)
            mailbox.add(FXCollections.<MailModel>observableArrayList());
    }
    
   
    public void setAccount(String account) throws RemoteException {
        remoteMailboxDataModel.setAccount(account);
    }
    
    public String getAccount() throws RemoteException {        
        return remoteMailboxDataModel.getAccount();
    }
    
    public ObservableList<mailclient.MailModel> getMailbox(int mailbox) throws RemoteException, AccountNotFoundException {
        this.mailbox.get(mailbox).clear();
        ArrayList<mailclient.MailModel> remoteMailbox = remoteMailboxDataModel
                                                      .getMailbox(mailbox);
        this.mailbox.get(mailbox).addAll(remoteMailbox);
        return this.mailbox.get(mailbox);

    }
    
    public boolean deleteMail(int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException {
        if(remoteMailboxDataModel.deleteMail(mailbox, mail))
            if(this.mailbox.get(mailbox).remove(mail))
                return true;

        return false;
    }
    
    public void sortMailbox(int mailbox, Comparator<? super MailModel> comparator) {
        this.mailbox.get(mailbox).sort(comparator);   
    }
    
    public boolean insertMail(int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException {
        return remoteMailboxDataModel.insertMail(mailbox, mail);
    }
    
    public boolean sendMail(MailModel mail) throws RemoteException, AccountNotFoundException {
        return remoteMailboxDataModel.sendMail(mail);
    }
    
}
