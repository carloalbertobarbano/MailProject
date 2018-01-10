/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Comparator;
import javafx.collections.ObservableList;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import mailserver.AccountNotFoundException;
import mailserver.Mailboxes;
import messaging.Intent;
import mylistsutils.Lists;
import mylistsutils.Predicate;
/**
 *
 * @author carloalberto
 */
public class RemoteMailboxDataModel implements IMailboxDataModel {
    private boolean initialized = false;
    
    private final List<ObservableList<MailModel>> mailbox = Collections.synchronizedList(new ArrayList<>());
    private mailserver.IRemoteMailboxDataModel remoteMailboxDataModel;
    
    private boolean useCache = true;
    
    private String account;
    private String cacheFolderPath; 
    private final ReentrantReadWriteLock[] cacheReadWriteLocks = new ReentrantReadWriteLock[Mailboxes.mailboxes_num];
    private final AtomicBoolean outboxHasPending = new AtomicBoolean(false);
    
    private HashMap<Integer, String> mailboxSorting = new HashMap<>();
    
    private SynchronizerThread syncThread = new SynchronizerThread();
    private boolean threadStarted = false;
    
    public class SynchronizerThread extends Thread { 
        //Thread for keeping mailboxes in sync
        //and sending mail in outbox 
        
        private boolean running = true;
        
        public void terminate() { running = false; }
        
        public void updateMailboxes() {
            for (int i = 0; i < Mailboxes.mailboxes_num; i++) {
                if (i == Mailboxes.MAILBOX_OUTBOX || i == Mailboxes.MAILBOX_TRASH)
                    continue;
                
                try {
                    ArrayList<MailModel> remoteMailbox = remoteMailboxDataModel.getMailbox(account, i);
                    
                    if (remoteMailbox == null)
                        continue;
                    
                    //System.out.println("Checking remote mailbox " + Mailboxes.labels.get(i));
                    //System.out.println("Remote mailbox has " + remoteMailbox.size() + " items");
                    
                    final int current_mailbox = i;
                    
                    
                     //Local removed mails
                    System.out.println("There are " + mailbox.get(Mailboxes.MAILBOX_TRASH).size() + " mails in the trash");
                    
                    List<MailModel> removedMails = Lists.filter(remoteMailbox, (rm) -> {
                        //System.out.println("Remote mail ID: " + rm.getId());
                        return Lists.satisfies(mailbox.get(Mailboxes.MAILBOX_TRASH), (tm) -> {
                            //System.out.println("Local mail ID: " + tm.getId());
                            //System.out.println("\tResult: " + tm.equals(rm));
                            return tm.equals(rm);
                        });
                    });
                  
                    removedMails.forEach(m -> {
                        System.out.println("Found new email to remove in " +
                                        Mailboxes.labels.get(current_mailbox) + ": " +
                                        m.getId()
                        );

                         try {
                            remoteMailboxDataModel.deleteMail(account, current_mailbox, m);

                        } catch (RemoteException | AccountNotFoundException e) {
                             e.printStackTrace();
                             System.err.println("Could not remove mail " + m.getId());
                        }
                            
                         
                        Platform.runLater(() -> {
                            mailbox.get(Mailboxes.MAILBOX_TRASH).remove(m);
                                
                            if (useCache)
                               saveMailboxToCache(
                                   cacheFolderPath + "/" + Mailboxes.labels.get(current_mailbox), 
                                   current_mailbox
                               );
                        }); 
                    });
                    
                    //Check for new emails
                    List<MailModel> newEmails = Lists.filter(remoteMailbox, rm -> {
                        return !mailbox.get(Mailboxes.MAILBOX_TRASH).contains(rm) && 
                                Lists.satisfiesAll(mailbox.get(current_mailbox), lm -> {
                                    return !lm.equals(rm);
                                });
                    });
                    
                    if (newEmails.size() > 0) {
                        for (MailModel m : newEmails) {
                            System.out.println("Received new email in " +
                                           Mailboxes.labels.get(current_mailbox) + ": " +
                                           m.getId()
                            );
                        }
                        
                        //Update client mailbox
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                //System.out.println("Adding mail");
                                mailbox.get(current_mailbox).clear(); //remove(0, mailbox.get(current_mailbox).size());
                                mailbox.get(current_mailbox).addAll(remoteMailbox);
                                mailbox.get(current_mailbox).sort(
                                        MailModel.comparators.get(mailboxSorting.get(current_mailbox))
                                );
                                
                                if (useCache)
                                    saveMailboxToCache(
                                        cacheFolderPath + "/" + Mailboxes.labels.get(current_mailbox), 
                                        current_mailbox
                                    );
                                
                                Intent newEmailIntent = new Intent(
                                                                FXMLMainActivityController.class,
                                                                FXMLMainActivityController.INTENT_ACTION_EMAIL_RECEIVED
                                                        );
                                newEmailIntent.putExtraInt("mailbox", current_mailbox);
                                
                                if (newEmails.size() == 1)
                                    newEmailIntent.putExtraString(
                                                                "message", 
                                                                newEmails.get(0).getSubject() + "\n" +
                                                                newEmails.get(0).getSender()
                                                   );
                                else
                                    newEmailIntent.putExtraString(
                                                                "message",
                                                                "You have " + newEmails.size() + " unread messages."
                                                   );
                                newEmailIntent.send();
                            }
                        });
                        
                        
                    }
                    
                    //Check for local email to sync
                    List<MailModel> localEmails = Lists.filter(mailbox.get(current_mailbox), new Predicate<Boolean, MailModel>() {
                        @Override
                        public Boolean apply(MailModel t) {
                            return !remoteMailbox.contains(t);
                        }
                    });
                    
                    if (localEmails.size() > 0 && remoteMailboxDataModel != null) {
                        for (MailModel m : localEmails) 
                            remoteMailboxDataModel.insertMail(account, current_mailbox, m);
                    }
                   
                } catch (RemoteException|AccountNotFoundException e) {
                    e.printStackTrace();
                    System.err.println("Could not fetch remote mailbox (" + Mailboxes.labels.get(i) + "): " +
                                        e);
                } 
            }
        }
        
        public void finalizeOutbox() {
            if (outboxHasPending.get()) {
                final AtomicBoolean result = new AtomicBoolean(true);
                
                for (MailModel m : mailbox.get(Mailboxes.MAILBOX_OUTBOX)) {
                    
                    Platform.runLater(() -> {
                        try {
                            remoteMailboxDataModel.sendMail(account, m);
                            deleteMail(Mailboxes.MAILBOX_OUTBOX, m);
                            mailbox.get(Mailboxes.MAILBOX_SENT).add(m);

                        } catch (RemoteException | AccountNotFoundException e) {
                            System.err.println("Could not send email from outbox: " + e);
                            result.set(false);
                        }
                    });  
                }

                if (result.get())
                    outboxHasPending.set(false);
            }
        }
        
        @Override
        public void run() {
            System.out.println("Started synchronizer thread");
            while(running) {
                
                updateMailboxes();
                finalizeOutbox();
               
                
                try {
                    Thread.sleep(2500);
                
                } catch (InterruptedException e) {
                    System.out.println("Sleep interrupted: " + e);
                }
            }
            System.out.println("Synchronizer thread finished");
        }
    }
    
    public RemoteMailboxDataModel(boolean useCache) {
        this.useCache = useCache;
        
        for (int i = 0; i < Mailboxes.mailboxes_num; i++) {
            mailbox.add(FXCollections.<MailModel>observableArrayList());
            if(useCache)
                cacheReadWriteLocks[i] = new ReentrantReadWriteLock();
            
            mailboxSorting.put(i, MailModel.SORT_DATE);
        }
    }
    
    public void initConnection() throws NotBoundException, MalformedURLException, RemoteException {
        if (initialized) return; 
        
        try {
            System.setProperty("java.security.policy", "file:/home/carloalberto/Documents/Università/prog3/MailProject/MailClient/src/mailclient/client.policy");

            if (System.getSecurityManager() == null)
                System.setSecurityManager(new SecurityManager());
            remoteMailboxDataModel = (mailserver.IRemoteMailboxDataModel)Naming.lookup("rmi://127.0.0.1:2000/mailserver");
        
        } catch (NotBoundException|MalformedURLException|RemoteException e) {
            System.out.println("Cannot connect to server, starting in offline mode");
            remoteMailboxDataModel = null;
            throw e;
        }
        
        initialized = true;
    }
    
    public void close() {
        syncThread.terminate();
    }
    
    
    public ArrayList<MailModel> loadMailboxFromCache(String path, int mailboxIndex) {
        ArrayList<MailModel> result = new ArrayList<>();
        
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        
        try {
            cacheReadWriteLocks[mailboxIndex].readLock().lock();
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);
            result = (ArrayList<MailModel>)ois.readObject();
            ois.close();
        
        } catch (FileNotFoundException e) {
            System.out.println("Cache file " + path + " not found");
            
        
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to load mailbox from cache file " + path);
        
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Class not found: " + e.getMessage());
        
        } finally {
            cacheReadWriteLocks[mailboxIndex].readLock().unlock();
        } 
        
        return result;
    }
    
    public void saveMailboxToCache(String path, int mailboxIndex) {
        
        try {
            cacheReadWriteLocks[mailboxIndex].writeLock().lock();
            System.out.println("Saving mailbox " + Mailboxes.labels.get(mailboxIndex)  + " to cache");
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            ArrayList<MailModel> temp = new ArrayList<>();
            this.mailbox.get(mailboxIndex).forEach((m) -> {
                temp.add(m);
            });
            
            oos.writeObject(temp);
            oos.close();
            System.out.println("Mailbox saved");
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not save mailbox to cache file " + path);
        
        } finally {
            cacheReadWriteLocks[mailboxIndex].writeLock().unlock();
        }
    }
    
    public void setAccount(String account) throws RemoteException {
        this.account = account;
        
        cacheFolderPath = System.getProperty("user.home") + "/cache_" + account.replace(".", "");
        
        if (useCache) {
            File cacheDir = new File(cacheFolderPath);
            if (!cacheDir.exists()) {
               if(cacheDir.mkdir())
                   System.out.println("Created cache folder at " + cacheFolderPath);
               else
                   System.err.println("Could not create cache folder at " + cacheFolderPath);
               
            } else 
                System.out.println("Found cache folder at " + cacheFolderPath);
            
            for (int i = 0; i < Mailboxes.mailboxes_num; i++) {
               ArrayList<MailModel> cachedMailbox = loadMailboxFromCache(String.format("%s/%s", 
                                                                                       cacheFolderPath,
                                                                                       Mailboxes.labels.get(i)), i);
               this.mailbox.get(i).setAll(cachedMailbox);
               
               if (i == Mailboxes.MAILBOX_OUTBOX && cachedMailbox.size() > 0)
                   outboxHasPending.set(true);
            }
        }
        
        if (!threadStarted) {
            threadStarted = true;
            syncThread.start();
        }
    }
    
    public String getAccount() throws RemoteException {
        return account;
    }
    
    public ObservableList<mailclient.MailModel> getMailbox(int mailboxIndex) {
        return this.mailbox.get(mailboxIndex);
    }
    
    public boolean deleteMail(int mailboxIndex, MailModel mail) throws RemoteException, AccountNotFoundException {
        if (mailboxIndex == Mailboxes.MAILBOX_OUTBOX) {
            this.mailbox.get(Mailboxes.MAILBOX_OUTBOX).remove(mail);
            if (useCache)
                    saveMailboxToCache(
                            cacheFolderPath + "/" + Mailboxes.labels.get(Mailboxes.MAILBOX_OUTBOX), 
                            Mailboxes.MAILBOX_OUTBOX
                    );
        } else {
            /*if(remoteMailboxDataModel != null && remoteMailboxDataModel.deleteMail(account, mailboxIndex, mail)) {
                if(this.mailbox.get(mailboxIndex).remove(mail)) {
                    if (useCache) {
                        saveMailboxToCache(
                                cacheFolderPath + "/" + Mailboxes.labels.get(mailboxIndex), 
                                mailboxIndex
                        );
                    return true;
                    }
                }
            } else {*/ //Move email to trash waiting to be synchronized
            System.out.println("Moving mail to Trash folder, id: " + mail.getId());
            if (mailboxIndex != Mailboxes.MAILBOX_TRASH)
                this.mailbox.get(Mailboxes.MAILBOX_TRASH).add(mail);
            
            this.mailbox.get(mailboxIndex).remove(mail);

            if (useCache)
                saveMailboxToCache(
                            cacheFolderPath + "/" + Mailboxes.labels.get(Mailboxes.MAILBOX_TRASH), 
                            Mailboxes.MAILBOX_TRASH
                );
            //}
        }
        
        return true;
    }
    
    public String getMailboxSorting(int mailboxIndex) {
        return this.mailboxSorting.get(mailboxIndex);
    }
    
    public void sortMailbox(int mailboxIndex, String comparator) {
        this.mailboxSorting.put(mailboxIndex, comparator);
        this.mailbox.get(mailboxIndex).sort(
                MailModel.comparators.get(comparator)
        );   
    }
    
    public boolean insertMail(int mailboxIndex, MailModel mail) throws RemoteException, AccountNotFoundException {
        
        if (useCache) {
            this.mailbox.get(mailboxIndex).add(mail);
            saveMailboxToCache(cacheFolderPath + "/" + Mailboxes.labels.get(mailboxIndex), 
                               mailboxIndex
            );
        }
        
        if (remoteMailboxDataModel != null)
            return remoteMailboxDataModel.insertMail(account, mailboxIndex, mail);
        else
            return false;
    }
    
    public boolean sendMail(MailModel mail) throws RemoteException, AccountNotFoundException {
        this.mailbox.get(Mailboxes.MAILBOX_OUTBOX).add(mail);
        outboxHasPending.set(true);
        
        if (useCache) {
            saveMailboxToCache(
                    cacheFolderPath + "/" + Mailboxes.labels.get(Mailboxes.MAILBOX_OUTBOX),
                    Mailboxes.MAILBOX_OUTBOX
            );
        }
        
        return true;
        //return remoteMailboxDataModel.sendMail(mail);
    }
    
}
