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
   
   private static IMailboxDataModel remoteInstance = null;
   private static IMailboxDataModel localInstance = null;
   
   public <T extends IMailboxDataModel> T getRemoteInstance() {
       if (remoteInstance == null) {
          remoteInstance = new RemoteMailboxDataModel(true);
       }
       
       return (T)remoteInstance;
   }
   
   public IMailboxDataModel getLocalInstance() {
       if (localInstance == null)
          localInstance = new MailboxDataModel();
       
       return localInstance;
       
   }
}
