/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Comparator;
import javafx.collections.ObservableList;

/**
 *
 * @author carloalberto
 */
public class RemoteMailboxDataModel extends UnicastRemoteObject implements IRemoteMailboxDataModel {
    
    public RemoteMailboxDataModel() throws RemoteException {
        super();
    }
    
    @Override
    public void setAccount(String account) throws RemoteException {
        
    }
    
    @Override
    public String getAccount() throws RemoteException {
        return "remoteaccount";
    }
    
    @Override
    public ObservableList<MailModel> getMailbox(int mailbox) throws RemoteException {
        return null;
    }
    
    @Override
    public boolean deleteMail(int mailbox, MailModel mail) throws RemoteException {
        return true;
    }
    
    @Override
    public boolean sortMailbox(int mailbox, Comparator<? super MailModel> comparator) throws RemoteException {
        return true;
    }
    
    @Override
    public boolean insertMail(int mailbox, MailModel mail) throws RemoteException {
        return true;
    }
}