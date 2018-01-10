/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import mylistsutils.Lists;

/**
 *
 * @author carloalberto
 */
public abstract class BroadcastReceiver {
    public static List<BroadcastReceiver> receivers = new ArrayList<>();
    
    public static void registerReceiver(BroadcastReceiver receiver) {
        receivers.add(receiver);
    }
    
    public abstract void onReceive(Intent intent);
}
