/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.util.ArrayList;

/**
 *
 * @author carloalberto
 */
interface Predicate<R, T> {
    public R apply(T t);
}

public class Lists {
    public static <T> ArrayList<T> filter(ArrayList<T> list, Predicate<Boolean, T> criteria) {
         ArrayList<T> result = new ArrayList<>();
         
         for (T t : list)
             if (criteria.apply(t))
                 result.add(t);
         
         return result;
    }
    
    public static <T> Boolean satisfies(ArrayList<T> list, Predicate<Boolean, T> criteria) {
        for (T t : list)
            if (criteria.apply(t))
                return true;
        return false;
    }
    
    public static <T> Boolean satisfiesAll(ArrayList<T> list, Predicate<Boolean, T> criteria) {
        for (T t : list)
            if (!criteria.apply(t))
                return false;
        return true;
    }
}
