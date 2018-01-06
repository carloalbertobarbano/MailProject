/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author carloalberto
 */
class TransactionManager {
    private final ReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = reentrantReadWriteLock.readLock();
    private final Lock writeLock = reentrantReadWriteLock.writeLock();
    
    private static TransactionManager instance = null;
    private static final String DATAFILE_PATH = "/home/carloalberto/mailserver.db";
    
  
  
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
    private JSONObject database;
    
    
    /*
        transactions[0] -> T1.id, BEGIN, null, null
        transactions[1] -> T1.id, INSERT, '/account/mailbox', 'mailobj'
        transactions[2] -> T1.id, END, null, null
    
        transactions[3] -> T2.id, BEGIN, null, null
        transactions[4] -> T2.id, READ, '/account/mailbox', null
        transactions[5] -> T3.id, END, null, null
    
    */
    private ArrayList<TransactionAction> transactions = new ArrayList<>();
    
    public static TransactionManager get() {
        if (instance == null)
            instance = new TransactionManager();
                    
        return instance;
    }
    
    
    
    private void readDatabase() throws IOException {
        if (Files.exists(Paths.get(DATAFILE_PATH))) {
            String content = Utils.readFile(DATAFILE_PATH, Charset.defaultCharset());
            database = new JSONObject(content);
            
        } else {
            Files.createFile(Paths.get(DATAFILE_PATH));
            database = new JSONObject();
        }
    }
    
    private boolean flush() throws IOException {
        Files.write(Paths.get(DATAFILE_PATH), database.toString().getBytes(), StandardOpenOption.CREATE);
        return true;
    }
    
    public TransactionManager() {
        try {
            readDatabase();
            
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    
    static class Transaction {
        private static int uniqueId = 0;
        private int id; 
        
        public Transaction() {
            id = uniqueId++;
        }
        
        public int getUniqueId() {
            return id;
        }
    }
    
    static class TransactionAction {
        public static final int BEGIN = 1;
        public static final int READ = 2;
        public static final int INSERT = 3;
        public static final int UPDATE = 4;
        public static final int DELETE = 5;
        public static final int END = 6;
        
        private Transaction t;
        private int action;
        private String path;
        private String data; 
        
        
        public TransactionAction(Transaction t, int action, String path, String data) {
            this.t = t; 
            this.action = action;
            this.path = path;
            this.data = data;
        }
        
        public Transaction getTransaction() { return t; }
        public int getAction() { return action; }
        public String getPath() { return path; }
        public String getData() { return data; }
    }
    
    public Transaction begin() {
        Transaction t = new Transaction();
        transactions.add(new TransactionAction(t, TransactionAction.BEGIN, null, null));
        return t;
    }
    
    public void execute(TransactionAction t) {
        transactions.add(t);
    }
    
   
    
    public int findJSONObjectIndexInArray(JSONArray array, JSONObject object) {
        for (int i = 0; i < array.length(); i++)
            if (array.getJSONObject(i).similar(object))
                return i;
            
        return -1;
    }
    
    public JSONObject applyTransactionActions(Transaction t) {
        //Retrieve all actions for transaction t
        ArrayList<TransactionAction> actions = Lists.filter(transactions, new Predicate<Boolean, TransactionAction>() {
            @Override
            public Boolean apply(TransactionAction ta) {
                return ta.getTransaction().getUniqueId() == t.getUniqueId() &&
                       ta.getAction() != TransactionAction.BEGIN && 
                       ta.getAction() != TransactionAction.END;
            }
        });
        
        JSONObject result = null;
        
        for (TransactionAction action : actions) {
            String keyAccount = action.getPath().split("/")[1];
            String keyMailbox = action.getPath().split("/")[2];
            JSONObject obj = null;
            if (action.getData() != null)
                obj = new JSONObject(action.getData());
            
            switch (action.getAction()) {
                case TransactionAction.READ:
                    readLock.lock();
                    result = database.getJSONObject(keyAccount);
                    readLock.unlock();
                    break;
                
                case TransactionAction.INSERT: 
                    writeLock.lock();
                    database.getJSONObject(keyAccount)
                            .getJSONArray(keyMailbox)
                            .put(new JSONObject(action.getData()));
                    writeLock.unlock();
                    break;
                
                case TransactionAction.UPDATE: 
                    writeLock.lock();
                    
                    writeLock.unlock();
                    break;
                    
                case TransactionAction.DELETE:
                    writeLock.lock();
                    int index = findJSONObjectIndexInArray(database.getJSONObject(keyAccount)
                                                                   .getJSONArray(keyMailbox), obj);
                                        
                    database.getJSONObject(keyAccount)
                            .getJSONArray(keyMailbox)
                            .remove(index);
                    writeLock.unlock();
                    break;
                    
                default: 
                    throw new RuntimeException("Unkown action " + action.getAction() + " for TransactionAction " + action);
            }
        }
        
        return result;
    }
    
    public JSONObject commit(Transaction t) {
        if (Lists.satisfies(transactions, new Predicate<Boolean, TransactionAction>() {
                                                @Override
                                                public Boolean apply(TransactionAction ta) {
                                                    return ta.getAction() == TransactionAction.END && 
                                                           ta.getTransaction().getUniqueId() == t.getUniqueId();
                                                }
                                          })) {
            throw new RuntimeException("Transaction " + t.getUniqueId() + " has already committed.");
        }
        
        transactions.add(new TransactionAction(t, TransactionAction.END, null, null));
        
        return applyTransactionActions(t);
    }
    
    public void abort(Transaction t) {
        transactions = Lists.filter(transactions, new Predicate<Boolean, TransactionManager.TransactionAction>() {
            @Override
            public Boolean apply(TransactionAction t2) {
                return t2.getTransaction().getUniqueId() != t.getUniqueId();
            }
        });
    }
    
}