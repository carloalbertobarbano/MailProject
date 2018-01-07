/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
/**
 * FXML Controller class
 *
 * @author carloalberto
 */
public class FXMLWriteActivityController implements Initializable {
    
    @FXML
    TextField textfield_to;
    
    @FXML
    TextField textfield_subject;
    
    @FXML
    TextArea textarea_body;
    
    @FXML
    Button button_send;
    
    @FXML
    Button button_discard;
    
    
    public boolean checkField(String field) {
        if (field == null) {
            new Alert(Alert.AlertType.ERROR, "All fields are required!", ButtonType.OK).show();
            return false;
        }
        
        return true;
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        button_discard.setOnAction((event) -> {
            ((Stage)button_discard.getScene().getWindow()).close();
        });
        
        button_send.setOnAction((event) ->  {
            try {
                ArrayList<String> dest = new ArrayList(Arrays.asList(textfield_to.getText().split(";")));
                String subject = textfield_subject.getText();
                String body = textarea_body.getText();
                String sender = new MailboxDataModelFactory().<RemoteMailboxDataModel>getRemoteInstance().getAccount();
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());

                if (dest.size() > 0 && !dest.get(0).isEmpty() && !subject.isEmpty() && !body.isEmpty() && !sender.isEmpty()) {

                    MailModel mailModel = new MailModel(sender, dest, subject, body, date);
                    new MailboxDataModelFactory().<RemoteMailboxDataModel>getRemoteInstance().sendMail(mailModel);

                    ((Stage)button_discard.getScene().getWindow()).close();
                } else {
                    new Alert(Alert.AlertType.ERROR, "All fields required!", ButtonType.OK).show();
                }
            
            } catch (Exception e) {
                FXMLMainActivityController.errorDialog(e.getMessage());
            }
        });
    }    
    
    public void initFromMail(MailModel mail) {
        textfield_to.setText(mail.getDest().toString()
                                        .replace("[","")
                                        .replace("]", "")
                                        .replace(",", ";"));
        textfield_subject.setText(mail.getSubject());
        textarea_body.setText(mail.getBody());
        
    }
}
