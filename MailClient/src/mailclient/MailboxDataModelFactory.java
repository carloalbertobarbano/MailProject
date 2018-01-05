/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

/**
 *
 * @author carloalberto
 */
public final class MailboxDataModelFactory {
   
   private static IMailboxDataModel instance = null;
   
   public IMailboxDataModel getInstance() {
       if (instance == null)
          instance = new RemoteMailboxDataModel();
       
       return instance;
   }
}
