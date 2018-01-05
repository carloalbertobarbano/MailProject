/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Comparator;
import javafx.collections.ObservableList;

/**
 *
 * @author carloalberto
 */
public interface IRemoteMailboxDataModel extends Remote {
    void setAccount(String account) throws RemoteException;
    String getAccount() throws RemoteException;
    ObservableList<mailclient.MailModel> getMailbox(int mailbox) throws RemoteException;
    boolean deleteMail(int mailbox, mailclient.MailModel mail) throws RemoteException;
    boolean sortMailbox(int mailbox, Comparator<? super mailclient.MailModel> comparator) throws RemoteException;
    boolean insertMail(int mailbox, mailclient.MailModel mail) throws RemoteException;
}