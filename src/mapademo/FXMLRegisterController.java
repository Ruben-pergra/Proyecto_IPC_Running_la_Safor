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
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.YEARS;
import javafx.scene.control.Button;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;


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
        emailError.setVisible(false);

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
        
        // Boton aceptar //
        BooleanBinding validFields = Bindings.and(validEmail, validPassword)
                 .and(confirmPasswords)
                  .and(validDate);
 
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
        boolean isValid = email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
        validEmail.set(isValid); //actualiza la property asociada
        showError(isValid, emailField, emailError); //muestra o esconde el mensaje de error
   }
    
    private void checkPassword() {
        String password = passwordField.getText();
        boolean isValid = password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,15}$");
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
        boolean isValid = value.isBefore(LocalDate.now().minus(16, YEARS));
        validDate.set(isValid);
        showError(isValid, dateField, dateError);
    }
    
    @FXML
    private void handleBAcceptOnAction(ActionEvent event) {
        emailField.clear();
        passwordField.clear();
        passwordConfirmField.clear();
        dateField.setValue(null);
        validEmail.setValue(Boolean.FALSE);
        validPassword.setValue(Boolean.FALSE);
        confirmPasswords.setValue(Boolean.FALSE);
        validDate.setValue(Boolean.FALSE);
    }
}