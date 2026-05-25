package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import upv.ipc.sportlib.SportActivityApp;

public class FXMLIniSesionController implements Initializable {

    @FXML
    private VBox rootVbox;
    
    @FXML
    private TextField nicknameField;
    @FXML
    private Label nicknameError;
    
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordError;
    
    @FXML
    private Button bAccept;
    @FXML
    private Button bCancel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nicknameError.setVisible(false);
        passwordError.setVisible(false);
        
        bCancel.setOnAction((event) -> {
            bCancel.getScene().getWindow().hide();
        });
    }
    
    private void showError(boolean isValid, Node field, Node errorMessage){
        errorMessage.setVisible(!isValid);
        field.setStyle(((isValid) ? "" : "-fx-background-color: #FCE5E0"));
    }

    @FXML
    private void handleBAcceptOnAction(ActionEvent event) {
        SportActivityApp app = SportActivityApp.getInstance();
        
        boolean loginCorrecto = app.login(nicknameField.getText(), passwordField.getText());
        
        if (loginCorrecto) {
            bAccept.getScene().getWindow().hide();
        } else {
            showError(false, nicknameField, nicknameError);
            showError(false, passwordField, passwordError);
            
            passwordField.clear();
            nicknameField.requestFocus();
        }
    }

    @FXML
    private void actualizarRootVbox(MouseEvent event) {
        rootVbox.requestFocus(); 
    }
}