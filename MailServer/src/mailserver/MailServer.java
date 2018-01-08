/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailserver;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author carloalberto
 */
public class MailServer extends Application {
    private TextArea textAreaLog; 
    
    @Override
    public void start(Stage primaryStage) {
        textAreaLog = new TextArea();
        textAreaLog.setEditable(false);
        
        StackPane root = new StackPane();
        root.getChildren().add(textAreaLog);
        
        Scene scene = new Scene(root, 500, 800);
        
        primaryStage.setTitle("Mail Server");
        primaryStage.setScene(scene);
        
        primaryStage.setOnCloseRequest(event -> {
            Logger.log("Exitting..");
            TransactionManager.get().close();
            System.exit(0);
        });
        
        primaryStage.show();
        
        Logger.textAreaLog = textAreaLog;
        
        Logger.log("Setting up security policies");
        System.setProperty("java.security.policy", "file:/home/carloalberto/Documents/Università/prog3/MailProject/MailServer/src/mailserver/server.policy");
        
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
       
        
        Logger.log("Loading RMI registry");
        
        try {
            LocateRegistry.createRegistry(2000);
            
        } catch (RemoteException e) {
            Logger.error("Exception creating RMI Registry: " + e);
        }
        
        
        try {
            RemoteMailboxDataModel remoteMailboxDataModel = new RemoteMailboxDataModel();
            Naming.rebind("rmi://127.0.0.1:2000/mailserver", remoteMailboxDataModel);
            Logger.log("Server bound: " + remoteMailboxDataModel);
            Logger.log("-------------------------------");
            
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { 
        launch(args);
    }
    
}
