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

/**
 *
 * @author carloalberto
 */
public interface IRemoteMailboxDataModel extends Remote {
    ArrayList<MailModel> getMailbox(String account, int mailbox) throws RemoteException, AccountNotFoundException;
    boolean deleteMail(String account, int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException;
    boolean insertMail(String account, int mailbox, MailModel mail) throws RemoteException, AccountNotFoundException;
    boolean sendMail(String account, MailModel mail) throws RemoteException, AccountNotFoundException;
}