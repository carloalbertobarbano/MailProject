/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mylistsutils;

import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author carloalberto
 */
public class Lists {
    public static <T> List<T> filter(List<T> list, Predicate<Boolean, T> criteria) {
         List<T> result = new ArrayList<>();
         
         synchronized (list) {
            for (T t : list)
                if (criteria.apply(t))
                    result.add(t);
         }
         
         return result;
    }
    
    public static <T> Boolean satisfies(List<T> list, Predicate<Boolean, T> criteria) {
        synchronized (list) {
            for (T t : list)
                if (criteria.apply(t))
                    return true;
        }
        
        return false;
    }
    
    public static <T> Boolean satisfiesAll(List<T> list, Predicate<Boolean, T> criteria) {
        synchronized (list) {
            for (T t : list)
                if (!criteria.apply(t))
                    return false;
        }
        
        return true;
    }
}
