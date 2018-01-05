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
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author carloalberto
 */
public class MailServer extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Setting up security policies");
        System.setProperty("java.security.policy", "file:/home/carloalberto/Documents/Università/prog3/MailProject/MailServer/src/mailserver/server.policy");
        //System.out.println(MailServer.class.getResource("server.policy"));
        
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
       
        
        System.out.println("Loading RMI registry");
        
        try {
            LocateRegistry.createRegistry(2000);
            
        } catch (RemoteException e) {
            System.out.println("Exception creating RMI Registry: " + e);
        }
        
        
        try {
            RemoteMailboxDataModel remoteMailboxDataModel = new RemoteMailboxDataModel();
            Naming.rebind("rmi://localhost:2000/mailserver", remoteMailboxDataModel);
            System.out.println("Server bound: " + remoteMailboxDataModel);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //launch(args);
    }
    
}
