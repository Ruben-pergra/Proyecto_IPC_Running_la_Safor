/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapademo;

import java.net.URL;

import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

public class FXMLInicioController implements Initializable {


    @Override
    public void initialize(URL url, ResourceBundle rb) {

        
    }

    @FXML
    private void OnRegistrarse(ActionEvent event) {
        try {
            // Cargamos el FXML del registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLRegister.fxml"));
            Parent root = loader.load();

            // Creamos una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Usuario - Running la Safor");

            // Hacerla modal para que no se pueda interactuar con la principal hasta cerrar esta
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.showAndWait();

            if (SportActivityApp.getInstance().getCurrentUser() != null) {
                ((javafx.scene.Node) (event.getSource())).getScene().getWindow().hide();

                FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
                Parent mainRoot = mainLoader.load();

                // Creamos una nueva ventana (Stage)
                Stage mainStage = new Stage();
                Scene mainScene = new Scene(mainRoot);
                mainScene.getStylesheets().add(
                        getClass().getResource("/resources/estilos.css").toExternalForm()
                );
                mainStage.setScene(mainScene);
                mainStage.setResizable(false);
                mainStage.setTitle("Aplicación principal - Running la Safor");

                mainStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el formulario de registro: " + e.getMessage());
        }
    }

    @FXML
    private void onIniSesion(ActionEvent event) {
        try {
            // Cargamos el FXML del registro
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLIniSesion.fxml"));
            Parent root = loader.load();

            // Creamos una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Inicio de seión - Running la Safor");

            // Hacerla modal para que no se pueda interactuar con la principal hasta cerrar esta
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.showAndWait();
            if (SportActivityApp.getInstance().getCurrentUser() != null) {
                ((javafx.scene.Node) (event.getSource())).getScene().getWindow().hide();

                FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
                Parent mainRoot = mainLoader.load();

                // Creamos una nueva ventana (Stage)
                Stage mainStage = new Stage();
                Scene mainScene = new Scene(mainRoot);
                mainScene.getStylesheets().add(
                        getClass().getResource("/resources/estilos.css").toExternalForm()
                );
                mainStage.setScene(mainScene);
                mainStage.setResizable(false);
                mainStage.setTitle("Aplicación principal - Running la Safor");

                mainStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el formulario de registro: " + e.getMessage());
        }
    }

}
