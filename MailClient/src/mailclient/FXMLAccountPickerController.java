/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import static mailclient.FXMLMainActivityController.errorDialog;

/**
 * FXML Controller class
 *
 * @author carloalberto
 */
public class FXMLAccountPickerController implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private ListView listview_account;
    
    @FXML
    private Button button_ok;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listview_account.setItems(FXCollections.observableArrayList(
                "carlo.alberto.barbano@outlook.com",
                "account2@email.com",
                "account3@email.com"
        ));
        
        button_ok.setOnAction(event -> {
            String account = (String)listview_account.getSelectionModel().getSelectedItem();
            System.out.println("Selected account " + account);
            
            Parent root;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLMainActivity.fxml"));
                root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                FXMLMainActivityController mainActivityController = loader.<FXMLMainActivityController>getController();
                mainActivityController.connectAccount(account);
                
                stage.setOnCloseRequest(closeEvent -> {
                    try {
                        new MailboxDataModelFactory().<RemoteMailboxDataModel>getRemoteInstance().close();

                    } catch (Exception e) {
                        errorDialog(e.getMessage());
                    }

                    Platform.exit();
                });
                   
                stage.show();
                
                ((Stage)button_ok.getScene().getWindow()).close();
                
            } catch (Exception e) {
                System.out.println("Exception starting FXMLWriteActivity: " + e.getMessage());
            }
        });
    }    
    
}
