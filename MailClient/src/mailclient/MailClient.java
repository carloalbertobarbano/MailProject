/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import static mailclient.FXMLMainActivityController.errorDialog;

/**
 *
 * @author carloalberto
 */
public class MailClient extends Application {
    
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLAccountPicker.fxml"));        
        
        Scene scene = new Scene(root);
        stage.setOnCloseRequest(event -> {
            try {
                new MailboxDataModelFactory().<RemoteMailboxDataModel>getRemoteInstance().close();

            } catch (Exception e) {
                errorDialog(e.getMessage());
            }
            
            Platform.exit();
        });
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
