/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import mailserver.AccountNotFoundException;
import mailserver.Mailboxes;
import mylistsutils.Lists;
import messaging.BroadcastReceiver;
import messaging.Intent;
/**
 *
 * @author carloalberto
 */
public class FXMLMainActivityController extends BroadcastReceiver implements Initializable {
    
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
    private Button button_reply;
    
    @FXML
    private Button button_reply_all;
    
    @FXML
    private Button button_forward;
    
    @FXML
    private ChoiceBox choicebox_sort;
    
    @FXML
    private Button button_get_messages;
    
    @FXML
    private Button button_write_message;
    
    @FXML
    private TextField textfield_search;
    
    private RemoteMailboxDataModel mailboxDataModel;
    private MailModel currentMail;
    private int currentMailbox;
    private String account; 
    
    public static final int INTENT_ACTION_EMAIL_RECEIVED = 1;
    
    public static void errorDialog(String message) {
        StackTraceElement stackTraceElements[] = Thread.currentThread().getStackTrace();
        String stackTrace = stackTraceElements[2].getClassName() + "." +
                            stackTraceElements[2].getMethodName() + ": " + 
                            stackTraceElements[2].getLineNumber();
        
        Alert alert = new Alert(Alert.AlertType.ERROR, message + "\n" + stackTrace, ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.showAndWait();
    } 
    
    private void replyHandler(boolean replyAll) {
        ArrayList<String> dest = null;
        
        if (replyAll) {
            dest = new ArrayList<>(currentMail.getDest());
            dest.remove(account);
            dest.add(currentMail.getSender());
        } else {
            dest = new ArrayList<>();
            dest.add(currentMail.getSender());
        }
        
        MailModel replyMail = new MailModel(
               account,
               dest,
               "RE: " + currentMail.getSubject(),
               "\n\n----------------------------\n" + currentMail.getSender() + " wrote: \n" +
               currentMail.getBody(),
               new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()),
               null
        );

        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLWriteActivity.fxml"));
            root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            FXMLWriteActivityController writeController = loader.<FXMLWriteActivityController>getController();
            writeController.initFromMail(replyMail);
            stage.show();

        } catch (Exception e) {
            System.out.println("Exception starting FXMLWriteActivity: " + e.getMessage());
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mailboxDataModel = new MailboxDataModelFactory().<RemoteMailboxDataModel>getRemoteInstance();
        
        try {
            mailboxDataModel.initConnection();
            
        } catch (Exception e) {
            errorDialog("Cannot connect to mailserver: " + e);
        }
        
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
        
        
        button_reply.setOnAction(event -> {
            replyHandler(false);
        });
        
        button_reply_all.setOnAction(event -> {
            replyHandler(true);
        });
        
        button_forward.setOnAction(event -> {
            MailModel forwardMail = new MailModel(
                    account,
                    new ArrayList<String>(),
                    "FW: " + currentMail.getSubject(),
                    "\n\n----------------------------\n" + currentMail.getSender() + " wrote: \n" +
                    currentMail.getBody(),
                    new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()),
                    null
            );
            
            Parent root;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLWriteActivity.fxml"));
                root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                FXMLWriteActivityController writeController = loader.<FXMLWriteActivityController>getController();
                writeController.initFromMail(forwardMail);
                stage.show();
                
            } catch (Exception e) {
                System.out.println("Exception starting FXMLWriteActivity: " + e.getMessage());
            }
        });  
        
        textfield_search.textProperty().addListener((observable, oldValue, query) -> {
            System.out.println("Filtering mailbox by " + query);
            
            FilteredList<MailModel> filtered = mailboxDataModel.getMailbox(currentMailbox).filtered(new Predicate<MailModel>() {
                @Override
                public boolean test(MailModel m) {
                    return m.getSender().toLowerCase().contains(query.toLowerCase()) || 
                           m.getSubject().toLowerCase().contains(query.toLowerCase()) || 
                           m.getBody().toLowerCase().contains(query.toLowerCase());
                }
            });
            list_view_messages.setItems(filtered);
        });
        
        BroadcastReceiver.registerReceiver(this);
    }
    
    public void connectAccount(String account) {
        this.account = account; 
        
        try {
            mailboxDataModel.setAccount(account);
            
        } catch (Exception e) {
            e.printStackTrace();
            errorDialog(e.toString());
        }
        
        
        TreeItem<String> accountTreeItem = new TreeItem<>(account);
        
        Mailboxes.labels.forEach(label -> accountTreeItem.getChildren().add(new TreeItem<>(label)));
        
        tree_nav.setRoot(accountTreeItem);
        tree_nav.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { 
             String mailbox = (String)((TreeItem)newValue).getValue();
             System.out.println("Selected: " + mailbox);
             
             
             if (mailbox.equals(account)) {
                 System.out.println("Selected account, skipping");
                 return;
             }
             
             try {
                currentMailbox = Mailboxes.labels.indexOf(mailbox);
                list_view_messages.setItems(mailboxDataModel.getMailbox(currentMailbox));
                mailboxDataModel.sortMailbox(
                        currentMailbox, 
                        mailboxDataModel.getMailboxSorting(currentMailbox)
                );
                
                if (mailboxDataModel.getMailboxSorting(currentMailbox).equals(MailModel.SORT_DATE)) {
                    choicebox_sort.getSelectionModel().select(0);
                } else {
                    choicebox_sort.getSelectionModel().select(1);
                }
                
             } catch (Exception e) {
                 System.out.println("Mailbox " + mailbox + " empty");
                 
             }
        });
               
        currentMailbox = Mailboxes.MAILBOX_INBOX;
        try {
            list_view_messages.setItems(mailboxDataModel.getMailbox(Mailboxes.MAILBOX_INBOX));
        
        } catch (Exception e) {
            //Empty mailbox
            System.out.println("Empty mailbox");
            
        }
        
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
                    currentMail = new_val;
                    webview_mail.getEngine().loadContent(new_val.getBody().replace("\n", "<br/>"));
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
            try {
                mailboxDataModel.deleteMail(currentMailbox, obj);
                
            } catch (Exception e) {
                errorDialog("Unable to delete mail: " + e.getMessage());
            }
            
            splitpane_mail.setDividerPositions(1.0);
        });
        
        choicebox_sort.getItems().addAll(MailModel.SORT_DATE, MailModel.SORT_SENDER);
        choicebox_sort.getSelectionModel().select(0);
        choicebox_sort.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
           public void changed(ObservableValue ov, Number value, Number new_value) {
                System.out.println("Selected " + new_value);
                
                switch (new_value.intValue()) {
                    case 0: //Date
                        mailboxDataModel.sortMailbox(currentMailbox, MailModel.SORT_DATE);
                        break;
                        
                    case 1: //Sender Name
                        mailboxDataModel.sortMailbox(currentMailbox, MailModel.SORT_SENDER);
                        break;
                        
                    default: break;
                }
           } 
        });
        mailboxDataModel.sortMailbox(currentMailbox, MailModel.SORT_DATE);
    }
    
    
    @Override
    public void onReceive(Intent intent) {
        if (intent.getAction() == INTENT_ACTION_EMAIL_RECEIVED) {
            if (intent.getExtraInt("mailbox") == Mailboxes.MAILBOX_INBOX) {
                new Alert(
                        Alert.AlertType.INFORMATION, 
                        "New email received\n" + intent.getExtraString("message"), 
                        ButtonType.OK
                ).show();
            }
        }
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