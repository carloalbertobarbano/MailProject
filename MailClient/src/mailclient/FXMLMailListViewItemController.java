/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * FXML Controller class
 *
 * @author carloalberto
 */
public class FXMLMailListViewItemController implements Initializable {
    @FXML
    private Text sender;
    
    @FXML
    private Text subject;
    
    @FXML
    private Label body;
    
    @FXML 
    private Text date;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void setModel(MailModel model) {
        try {
            if (model.getSender().equals(new MailboxDataModelFactory().<RemoteMailboxDataModel>getRemoteInstance().getAccount()))
                this.sender.setText(model.getDest().toString());
            else
                this.sender.setText(model.getSender());
        } catch (Exception e) {
            this.sender.setText(model.getSender());
        }
        this.subject.setText(model.getSubject());
        this.body.setText(model.getBody());
        this.date.setText(model.getDate());
    }
    
}
