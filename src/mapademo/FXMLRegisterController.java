/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapademo;


import java.net.URL;

import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.Node;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import javafx.scene.control.Button;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import upv.ipc.sportlib.User;
import upv.ipc.sportlib.SportActivityApp;

public class FXMLRegisterController implements Initializable {
    // Email //
    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;   
    
    private BooleanProperty validEmail;
    private ChangeListener<String> listenerEmail;
    
    // Contraseña //
    @FXML
    private TextField passwordField;
    @FXML
    private Label passwordError;

    private BooleanProperty validPassword;
    private ChangeListener<String> listenerPassword;
    
    // Contraseña_2 //
    @FXML
    private PasswordField passwordConfirmField;
    @FXML
    private Label passwordConfirmError;

    private BooleanProperty confirmPasswords;
    private ChangeListener<String> listenerConfirmPassword;
    
    // Fecha //
    @FXML
    private DatePicker dateField;
    @FXML
    private Label dateError;

    private BooleanProperty validDate;
    private ChangeListener<LocalDate> listenerDate;
    
    // boton aceptar //
    @FXML
    private Button bAccept;
    
    // boton cancelar //
    @FXML
    private Button bCancel;
    
    // Nickname//
    @FXML
    private TextField nicknameField;
    @FXML
    private Label nicknameError;
    
    private BooleanProperty validNickname;
    private ChangeListener<String> listenerNickname;
    
    //Avatar//
    @FXML
    private ImageView avatarImageView;
    //Para guardar la ruta q irá luego a la librería
    private String selectedAvatarPath = "";
    
    //RootVbox//
    @FXML 
    private VBox rootVbox;
    
    private void showError(boolean isValid, Node field, Node errorMessage){
        errorMessage.setVisible(!isValid);
        field.setStyle(((isValid) ? "" : "-fx-background-color: #FCE5E0"));
    }

    //=========================================================
    // you must initialize here all related with the object 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Email //
        validEmail = new SimpleBooleanProperty();
        validEmail.setValue(Boolean.FALSE);
        validNickname = new SimpleBooleanProperty();
        validNickname.setValue(Boolean.FALSE);
        emailError.setVisible(false);
        nicknameError.setVisible(false);

        //Check values when user leaves edits
        emailField.focusedProperty().addListener((observable, oldValue, newValue)->{
        if(!newValue){ //focus lost
            checkEmail();
            if (!validEmail.get()) {
                if (listenerEmail == null) {
                    listenerEmail = (a, b, c) -> checkEmail();
                    emailField.textProperty().addListener(listenerEmail);
                    }
                }
            }
        });
        
        // Contraseña //
        validPassword = new SimpleBooleanProperty(false); 
        passwordError.setVisible(false);

        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkPassword();

                if (!validPassword.get()) {
                    if (listenerPassword == null) {
                        listenerPassword = (obs, oldVal, newVal) -> checkPassword();
                        passwordField.textProperty().addListener(listenerPassword);
                    }
                }
            }
        });
        
        // Contraseña_2 //
        confirmPasswords = new SimpleBooleanProperty(false);
        passwordConfirmError.setVisible(false);

        passwordConfirmField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkPasswordsMatch();

                if (!confirmPasswords.get()) {
                    if (listenerConfirmPassword == null) {
                        listenerConfirmPassword = (obs, oldVal, newVal) -> {
                            boolean m = passwordField.getText().equals(newVal);
                            confirmPasswords.set(m);
                            showError(m, passwordConfirmField, passwordConfirmError);
                        };
                        passwordConfirmField.textProperty().addListener(listenerConfirmPassword);
                    }
                }
            }
        });
        
        // Fecha //
        validDate = new SimpleBooleanProperty(false);
        dateError.setVisible(false);

        dateField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                checkDate();

                if (!validDate.get()) {
                    if (listenerDate == null) {
                        listenerDate = (obs, oldVal, newVal) -> checkDate();
                        dateField.valueProperty().addListener(listenerDate);
                    }
                }  
            }
        });
        
        //Nickname//
        nicknameField.focusedProperty().addListener((obs, oldV, newV) -> {
            if(!newV){ //focus lost
            checkNickname();
            if (!validNickname.get()) {
                if (listenerNickname == null) {
                    listenerNickname = (a, b, c) -> checkNickname();
                    nicknameField.textProperty().addListener(listenerNickname);
                    }
                }
            }
        });
        
        // Boton aceptar //
        BooleanBinding validFields = Bindings.and(validEmail,validNickname)
                .and(confirmPasswords)
                .and(validDate)
                .and (validPassword);
 
        bAccept.disableProperty().bind(
                        Bindings.not(validFields)
                   );
        
        // Boton cancelar //
        bCancel.setOnAction( (event)->{
            bCancel.getScene().getWindow().hide();
            });
        
    }
    private void checkEmail(){
        String email = emailField.getText();
        boolean isValid = User.checkEmail(email);
        validEmail.set(isValid); //actualiza la property asociada
        showError(isValid, emailField, emailError); //muestra o esconde el mensaje de error
   }
    
    private void checkPassword() {
        String password = passwordField.getText();
        boolean isValid = User.checkPassword(password);
        validPassword.set(isValid); //actualiza la property asociada
        showError(isValid, passwordField, passwordError); //muestra o esconde el mensaje de error
    }
    
    private void checkPasswordsMatch() {
        boolean match = passwordField.getText().equals(passwordConfirmField.getText());

        confirmPasswords.set(match);
        showError(match, passwordConfirmField, passwordConfirmError);

        if (!match) {
            passwordField.clear();
            passwordConfirmField.clear();
            passwordField.requestFocus();
        }
    }
    
    private void checkDate(){
        LocalDate value = dateField.getValue();
        boolean isValid = (value != null) && User.isOlderThan(value, 12);
        validDate.set(isValid);
        showError(isValid, dateField, dateError);
    }
    
    private void checkNickname() {
    String nick = nicknameField.getText();
    boolean isValid = User.checkNickName(nick);
    validNickname.set(isValid);
    showError(isValid, nicknameField, nicknameError);
    }
    
    @FXML
    private void handleBAcceptOnAction(ActionEvent event) {
        //Obtenemos la instancia única para conectar con la base de datos
        SportActivityApp app = SportActivityApp.getInstance();
        boolean success = app.registerUser(
        nicknameField.getText(),
        emailField.getText(),
        passwordField.getText(),
        dateField.getValue(),
        selectedAvatarPath
        );
        if (success) {
            bAccept.getScene().getWindow().hide();
            app.login(nicknameField.getText(), passwordField.getText());
        } else{
            nicknameError.setText("El nickname '" + nicknameField.getText() + "' ya está en uso.");
            showError(false, nicknameField, nicknameError);
            nicknameField.requestFocus();
        }
    
        
    }
    
    @FXML
    private void pickAvatar(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Perfil");

        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(avatarImageView.getScene().getWindow());

        if (selectedFile != null) {
            selectedAvatarPath = selectedFile.getAbsolutePath();

            Image image = new Image(selectedFile.toURI().toString());
            avatarImageView.setImage(image);
        }
    }
    
    @FXML
    private void actualizarRootVbox(MouseEvent event) {
        rootVbox.requestFocus(); 
    }
}