/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import mailclient.MailModel;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.collections.ObservableList;

/**
 *
 * @author carloalberto
 */
public interface IRemoteMailboxDataModel extends Remote {
    void setAccount(String account) throws RemoteException;
    String getAccount() throws RemoteException;
    ArrayList<MailModel> getMailbox(int mailbox) throws RemoteException, AccountNotFoundException;
    boolean deleteMail(int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException;
    boolean insertMail(int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException;
    boolean sendMail(MailModel mail) throws RemoteException, AccountNotFoundException;
}