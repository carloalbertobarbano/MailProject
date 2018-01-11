/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import mylistsutils.Lists;
import mylistsutils.Predicate;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.atomic.AtomicBoolean;
import mailclient.MailModel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author carloalberto
 */
public class TransactionManager {
    private final ReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = reentrantReadWriteLock.readLock();
    private final Lock writeLock = reentrantReadWriteLock.writeLock();
    
    private static TransactionManager instance = null;
    private static final String DATAFILE_PATH = "/home/carloalberto/mailserver.json";
    
    /*
    {
        email@address: {
            inbox: [ {mailobj}, {mailobj} ],
            sent:  [ {mailobj}, {mailobj} ],
            ....
        },
        email2@address: {
            inbox: [ {mailobj}, {mailobj} ],
    
            sent:  [ {mailobj}, {mailobj} ],
            ....
        }
    }
    
    */
    private HashMap<String, HashMap<String, ArrayList<mailclient.MailModel>>> database;
    
    
    /*
        transactions[0] -> T1.id, BEGIN, null, null
        transactions[1] -> T1.id, INSERT, '/account/mailbox', 'mailobj'
        transactions[2] -> T1.id, END, null, null
    
        transactions[3] -> T2.id, BEGIN, null, null
        transactions[4] -> T2.id, READ, '/account/mailbox', null
        transactions[5] -> T3.id, END, null, null
    
    */
    private List<TransactionAction> transactions = Collections.synchronizedList(new ArrayList<>());
    private AtomicBoolean needsFlush = new AtomicBoolean(false);
    private FlusherThread flusherThread = new FlusherThread();
    
    private void dumpTransactions() {
        String actions[] = {"BEGIN", "READ", "INSERT", "UPDATE", "DELETE", "END", "ABORT"};
        
        TransactionAction trans[] = transactions.toArray(new TransactionAction[0]);
        
        for (TransactionAction t : trans)  {
            int id = t.getTransaction().getUniqueId();
            int action = t.getAction();
            String actionString = actions[action];
            String path = t.getPath();
            Logger.log("Transaction " + id + ": " + actionString + ", " + path);
        }
    }
    
    public class FlusherThread extends Thread {
        private boolean running = true;
        
        private boolean flush(boolean force) throws IOException {
            //Logger.log("in flush");
            if (needsFlush.get()) {

                //Flush if there is no currently active transaction modifying the database
                List<TransactionAction> startedTransactions = force ? null : Lists.filter(transactions, ta -> {
                    return ta.getAction() == TransactionAction.BEGIN;
                });

                List<TransactionAction> finishedTransactions = force ? null : Lists.filter(transactions, ta -> {
                    return ta.getAction() == TransactionAction.END || ta.getAction() == TransactionAction.ABORT;
                });

                Logger.log("Active transactions: " + (startedTransactions.size() - finishedTransactions.size()));
                dumpTransactions();
                // unless forced, continue only if all transactions have ended
                // We can just compare the sizes, as it is guaranteed that a transaction 
                // can commit only once (see TransactionManager.commit())
                if (!(force || startedTransactions.size() == finishedTransactions.size()))
                    return false;
                
                Logger.log("Flushing database to persistent file");
                writeLock.lock();

                try {
                    JSONObject jsonDatabase = new JSONObject(database);
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(DATAFILE_PATH))) {
                        out.write(jsonDatabase.toString());
                    }
                    Logger.log("Flushing done");
                    needsFlush.set(false);

                } catch (IOException e) {
                    throw e;

                } finally {
                    writeLock.unlock();
                }
            }

            return true;
        }
        
        public void terminate() { running = false; }
        
        @Override
        public void run() {
            Logger.log("Starting flusher thread");
            
            while(running) {
                try {
                    flush(false);
                    Thread.sleep(500);

                } catch (IOException e) {
                    Logger.error("Could not flush database: " + e.getMessage());
                } catch (InterruptedException e) {

               }
            }
            
            try {
                flush(true); //Force flush (partial transactions) when exitting

            } catch (IOException e) {
                Logger.error("Could not flush database: " + e.getMessage());
            }  
            
            Logger.log("Exitting flusher thread");
        }
    }
    
    public TransactionManager() {
        
    }
    
    public void init() {
        try {
            readDatabase();
            flusherThread.start();
            
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public void close() {
        flusherThread.terminate();
    }
    
    public static TransactionManager get() {
        if (instance == null) {
            instance = new TransactionManager();
            instance.init();
        }
        
        return instance;
    }
    
    private void readDatabase() throws IOException {
        database = new HashMap<>();
        
        if (Files.exists(Paths.get(DATAFILE_PATH))) {
            String content = Utils.readFile(DATAFILE_PATH, Charset.defaultCharset());
            JSONObject json = new JSONObject(content);
            
            Set<String> accounts = json.keySet();
            for (String account : accounts) { //Retrieve all accounts
                HashMap<String, ArrayList<mailclient.MailModel>> mailboxes = new HashMap<>();
                
                JSONObject jsonMailboxes = json.getJSONObject(account);
                Set<String> mailboxesNames = jsonMailboxes.keySet();
                
                for (String mailboxName : mailboxesNames) { //Retrieve all account's mailboxes
                    ArrayList<mailclient.MailModel> mails = new ArrayList<>();
                    JSONArray jsonMails = jsonMailboxes.getJSONArray(mailboxName);
                    
                    for (int mailIndex = 0; mailIndex < jsonMails.length(); mailIndex++) { //Retrieve all mailbox's mails
                        JSONObject jsonMailObject = jsonMails.getJSONObject(mailIndex);
                        
                        String sender = jsonMailObject.getString("sender");
                        ArrayList<String> dest = new ArrayList<>();
                        JSONArray jsonArrayDest = jsonMailObject.getJSONArray("dest");
                        for (int destIndex = 0; destIndex < jsonArrayDest.length(); destIndex++)
                            dest.add(jsonArrayDest.getString(destIndex));

                        String subject = jsonMailObject.getString("subject");
                        String body = jsonMailObject.getString("body");
                        String date = jsonMailObject.getString("date");
                        String id = jsonMailObject.getString("id");
                        Logger.log("Read mail from DB with ID " + id);
                        mails.add(new MailModel(sender, dest, subject, body, date, id));
                    }
                    
                    mailboxes.put(mailboxName, mails);
                }
                
                database.put(account, mailboxes);
            }
            
        } else {
            //Database is empty 
            //TODO: Create default account/mailbox set here
            Files.createFile(Paths.get(DATAFILE_PATH));
            database = new HashMap<>();
        }
    }
    
    public ArrayList<MailModel> applyTransactionActions(Transaction t) throws IOException, AccountNotFoundException {
        
        //Retrieve all actions for transaction t
        List<TransactionAction> actions = Lists.filter(transactions, new Predicate<Boolean, TransactionAction>() {
            @Override
            public Boolean apply(TransactionAction ta) {
                return ta.getTransaction().getUniqueId() == t.getUniqueId() &&
                       ta.getAction() != TransactionAction.BEGIN && 
                       ta.getAction() != TransactionAction.END;
            }
        });
        
        ArrayList<MailModel> result = null;
        
        
        for (TransactionAction action : actions) {
            String keyAccount = null;
            String keyMailbox = null; 
            try {
                keyAccount = action.getPath().split("/")[1];
                keyMailbox = action.getPath().split("/")[2];
            
            } catch (Exception e) {
                Logger.error("Malformed path : " + action.getPath());
                throw new IllegalArgumentException("Malformed path: " + action.getPath());
            }
            
            if (!database.containsKey(keyAccount)) {
                Logger.warning("Account " + keyAccount + " not found");
                throw new AccountNotFoundException("Account " + keyAccount + " not found");
            }
                
            
            switch (action.getAction()) {
                case TransactionAction.READ:
                    readLock.lock();
                    result = database.get(keyAccount).get(keyMailbox);
                    readLock.unlock();
                    break;
                
                case TransactionAction.INSERT: 
                    writeLock.lock();
                    if (!database.get(keyAccount).containsKey(keyMailbox)) {
                        if (!Mailboxes.labels.contains(keyMailbox)) {
                            Logger.error("Invalid mailbox: " + keyMailbox);
                            throw new IllegalArgumentException("Invalid mailbox: " + keyMailbox);
                        }
                        
                        Logger.log(action.getPath() + " not found. Inserting it");
                        database.get(keyAccount).put(keyMailbox, new ArrayList<mailclient.MailModel>());
                    }
                    
                    database.get(keyAccount)
                            .get(keyMailbox)
                            .add(action.getNewValue());
                    
                    needsFlush.set(true);
                    writeLock.unlock();
                    break;
                
                case TransactionAction.UPDATE: 
                    writeLock.lock();
                    int index = database.get(keyAccount)
                                        .get(keyMailbox)
                                        .indexOf(action.getOldValue());
                    database.get(keyAccount)
                            .get(keyMailbox)
                            .set(index, action.getNewValue());
                    needsFlush.set(true);
                    writeLock.unlock();
                    break;
                    
                case TransactionAction.DELETE:
                    writeLock.lock();
                          
                    database.get(keyAccount)
                            .get(keyMailbox)
                            .remove(action.getOldValue());
                    
                    needsFlush.set(true);
                    writeLock.unlock();
                    break;
                    
                default: 
                    Logger.error("Unkown action: " + action.getAction() + " for TransactionAction " + action);
                    throw new RuntimeException("Unkown action " + action.getAction() + " for TransactionAction " + action);
            }
        }
        
        transactions.add(new TransactionAction(t, TransactionAction.END, null, null, null));
        
        //Remove records for this transaction, since it has completed 
        //We are not logging transactions for after-crash restore, so we dont need it
        /*synchronized (transactions) {
            transactions = Lists.filter(transactions, new Predicate<Boolean, TransactionAction>() {
                @Override
                public Boolean apply(TransactionAction t2) {
                    return t2.getTransaction().getUniqueId() != t.getUniqueId();
                }
            });
        }*/
        
        return result;
    }
    
    public void revertTransactionActions(Transaction t) {
        Logger.log("Reverting actions for transaction: " + t.getUniqueId());
        
        //Retrieve all actions for transaction t
        List<TransactionAction> actions = Lists.filter(transactions, new Predicate<Boolean, TransactionAction>() {
            @Override
            public Boolean apply(TransactionAction ta) {
                return ta.getTransaction().getUniqueId() == t.getUniqueId() &&
                       ta.getAction() != TransactionAction.BEGIN && 
                       ta.getAction() != TransactionAction.END;
            }
        });
        
        for (TransactionAction action : actions) {
            String keyAccount = null;
            String keyMailbox = null; 
            try {
                keyAccount = action.getPath().split("/")[1];
                keyMailbox = action.getPath().split("/")[2];
            
            } catch (Exception e) {
                Logger.error("Malformed path : " + action.getPath());
                throw new IllegalArgumentException("Malformed path: " + action.getPath());
            }
            
            if (!database.containsKey(keyAccount)) {
                Logger.warning("Account " + keyAccount + " not found");
                continue;
            }
                
            
            switch (action.getAction()) {
                case TransactionAction.INSERT: 
                    writeLock.lock();
                    if (!database.get(keyAccount).containsKey(keyMailbox)) {
                        if (!Mailboxes.labels.contains(keyMailbox)) {
                            Logger.error("Invalid mailbox: " + keyMailbox);
                            throw new IllegalArgumentException("Invalid mailbox: " + keyMailbox);
                        }
                        
                        Logger.log(action.getPath() + " not found. Inserting it");
                        database.get(keyAccount).put(keyMailbox, new ArrayList<mailclient.MailModel>());
                    }
                    
                    database.get(keyAccount)
                            .get(keyMailbox)
                            .remove(action.getNewValue());
                    
                    needsFlush.set(true);
                    writeLock.unlock();
                    break;
                
                case TransactionAction.UPDATE: 
                    writeLock.lock();
                    int index = database.get(keyAccount)
                                        .get(keyMailbox)
                                        .indexOf(action.getNewValue());
                    database.get(keyAccount)
                            .get(keyMailbox)
                            .set(index, action.getOldValue());
                    needsFlush.set(true);
                    writeLock.unlock();
                    break;
                    
                case TransactionAction.DELETE:
                    writeLock.lock();
                          
                    database.get(keyAccount)
                            .get(keyMailbox)
                            .add(action.getOldValue());
                    
                    needsFlush.set(true);
                    writeLock.unlock();
                    break;
                    
                default: 
                    Logger.error("Unkown action: " + action.getAction() + " for TransactionAction " + action);
                    throw new RuntimeException("Unkown action " + action.getAction() + " for TransactionAction " + action);
            }
        }
        
        transactions.add(new TransactionAction(t, TransactionAction.ABORT, null, null, null));
        
        //Remove records for this transaction, since it has completed 
        //We are not logging transactions for after-crash restore, so we dont need it
        /*synchronized (transactions) {
            transactions = Lists.filter(transactions, new Predicate<Boolean, TransactionAction>() {
                @Override
                public Boolean apply(TransactionAction t2) {
                    return t2.getTransaction().getUniqueId() != t.getUniqueId();
                }
            });
        }*/
    }
    
    public Transaction begin() {
        Transaction t = new Transaction();
        transactions.add(new TransactionAction(t, TransactionAction.BEGIN, null, null, null));
        return t;
    }
    
    public void execute(TransactionAction t) {
        transactions.add(t);
    }
    
    public ArrayList<MailModel> commit(Transaction t) throws IOException, AccountNotFoundException {
        if (Lists.satisfies(transactions, new Predicate<Boolean, TransactionAction>() {
                                                @Override
                                                public Boolean apply(TransactionAction ta) {
                                                    return ta.getAction() == TransactionAction.END && 
                                                           ta.getTransaction().getUniqueId() == t.getUniqueId();
                                                }
                                          })) {
            throw new RuntimeException("Transaction " + t.getUniqueId() + " has already committed.");
        }
        
        return applyTransactionActions(t);
    }
    
    public void abort(Transaction t) {
        revertTransactionActions(t);
    }
    
}