/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import javafx.stage.Stage;
import javafx.scene.Scene;

/**
 *
 * @author carloalberto
 */
public class FXMLMainActivityController implements Initializable {
    
    @FXML
    private TreeView tree_nav;
    
    @FXML
    private ListView list_view_messages;
    
    @FXML
    private SplitPane splitpane_mail;
    
    @FXML
    private WebView webview_mail;
    
    @FXML
    private Button button_close_webview;
    
    @FXML
    private Text text_sender;
    
    @FXML
    private Text text_dest;
    
    @FXML
    private Text text_subject;
    
    @FXML
    private Text text_date;
    
    @FXML
    private Button button_delete;
    
    @FXML
    private ChoiceBox choicebox_sort;
    
    @FXML
    private Button button_get_messages;
    
    @FXML
    private Button button_write_message;
    
    private IMailboxDataModel mailboxDataModel;
    private int currentMailbox;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mailboxDataModel = new MailboxDataModelFactory().getInstance();
        mailboxDataModel.setAccount("carlo.alberto.barbano@outlook.com");
        
        TreeItem<String> account = new TreeItem<>(mailboxDataModel.getAccount());
        
        MailboxDataModel.Mailboxes.labels.forEach(label -> account.getChildren().add(new TreeItem<>(label)));
        
        tree_nav.setRoot(account);
        tree_nav.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { 
             String mailbox = (String)((TreeItem)newValue).getValue();
             System.out.println("Selected: " + mailbox);
             
             
             if (mailbox.equals(mailboxDataModel.getAccount())) {
                 System.out.println("Selected account, skipping");
                 return;
             }
             
             try {
                int index = IMailboxDataModel.Mailboxes.labels.indexOf(mailbox);
                list_view_messages.setItems(mailboxDataModel.getMailbox(index));
                currentMailbox = index;
                mailboxDataModel.sortMailbox(currentMailbox, MailModel.SortDate);
                
             } catch (Exception e) {
                 System.out.println("Mailbox " + mailbox + " not in labels");
             }
             
        });
               
        currentMailbox = MailboxDataModel.Mailboxes.MAILBOX_INBOX;
        list_view_messages.setItems(mailboxDataModel.getMailbox(MailboxDataModel.Mailboxes.MAILBOX_INBOX));
        list_view_messages.setCellFactory(new Callback<ListView<MailModel>, ListCell<MailModel>>() {
                                            @Override
                                            public ListCell<MailModel> call(ListView<MailModel> list) {
                                                MyListCell cell = new MyListCell();
                                                return cell;
                                            }
        });
        
        list_view_messages.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<MailModel>() {
                public void changed(ObservableValue<? extends MailModel> ov, MailModel old_val, MailModel new_val) {
                    if (new_val == null) return;
                    
                    webview_mail.getEngine().loadContent(new_val.getBody());
                    text_sender.setText(new_val.getSender());
                    text_subject.setText(new_val.getSubject());
                    text_dest.setText(new_val.getDest().toString());
                    text_date.setText(new_val.getDate());
                    splitpane_mail.setDividerPositions(0.3);
                }
        });
        
        splitpane_mail.setDividerPositions(1.0);
        
        button_close_webview.setOnAction((event) -> {
            splitpane_mail.setDividerPositions(1.0);
        });
        
        button_delete.setOnAction((event) -> {
            MailModel obj = (MailModel)list_view_messages.getSelectionModel().getSelectedItem();
            System.out.println("Deleting selected email: " + obj.getId());
            mailboxDataModel.deleteMail(currentMailbox, obj);
            splitpane_mail.setDividerPositions(1.0);
        });
        
        choicebox_sort.getItems().addAll(MailModel.SORT_DATE, MailModel.SORT_SENDER);
        choicebox_sort.getSelectionModel().select(0);
        choicebox_sort.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
           public void changed(ObservableValue ov, Number value, Number new_value) {
                System.out.println("Selected " + new_value);
                
                switch (new_value.intValue()) {
                    case 0: //Date
                        mailboxDataModel.sortMailbox(currentMailbox, MailModel.SortDate);
                        break;
                        
                    case 1: //Sender Name
                        mailboxDataModel.sortMailbox(currentMailbox, MailModel.SortSender);
                        break;
                        
                    default: break;
                }
           } 
        });
        mailboxDataModel.sortMailbox(currentMailbox, MailModel.SortDate);
        
        
        
        button_write_message.setOnAction(event -> {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("FXMLWriteActivity.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
                
            } catch (Exception e) {
                System.out.println("Exception starting FXMLWriteActivity: " + e.getMessage());
            }
        });
    }    
    
}


class MyListCell<T> extends ListCell<T> {
      @Override
      public void updateItem(T item, boolean empty) {
          super.updateItem(item, empty);
          
          if (empty) {
              setText(null);
              setGraphic(null);
              
          } else {
              try {
                FXMLMailListViewItemController controller;
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLMailListViewItem.fxml"));
                Node graphic = loader.load();
                controller = loader.getController();
                controller.setModel((MailModel)item);
                setGraphic(graphic);
                
              } catch (IOException e) {
                  throw new RuntimeException(e);
              }
          }
      }
}