/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.HashMap;
import java.util.List;
import mylistsutils.Lists;

/**
 *
 * @author carloalberto
 */
public class Intent {
    private int action;
    private HashMap<String, Integer> integers = new HashMap<>();
    private HashMap<String, String> strings   = new HashMap<>();
    private HashMap<String, Object> objects   = new HashMap<>();
    private final Class<?> recv;
    
    public Intent(Class<?> c, int action) {
        this.recv = c;
        this.action = action;
    }   
    
    public int getAction() { return action; }
    
    public void putExtraInt(String k, Integer i) { integers.put(k, i); }
    public void putExtraString(String k, String s) { strings.put(k, s); }
    public void putExtraObject(String k, Object o) { objects.put(k, o); }
    
    public int getExtraInt(String k) { return integers.get(k); }
    public String getExtraString(String k) { return strings.get(k); }
    public Object getExtraObject(String k) { return objects.get(k); }
    
    public void send() {
        System.out.println("Sending intent, there are " + BroadcastReceiver.receivers.size() + " receivers");
        List<BroadcastReceiver> receivers = Lists.filter(BroadcastReceiver.receivers, b -> {
            return recv.isInstance(b);
        });
        System.out.println("Found " + receivers.size() + " matching receivers");
        
        receivers.forEach(r -> { r.onReceive(this); });
    }
    
}
