/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;
import upv.ipc.sportlib.MapRegion;      
import upv.ipc.sportlib.SportActivityApp;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


/**
 * FXML Controller class
 *
 * @author charlottediaz
 */
public class FXMLMapaController implements Initializable {

    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private GridPane formGrid;
    @FXML
    private TextField latMaxField;
    @FXML
    private TextField latMinField;
    @FXML
    private TextField lonMaxField;
    @FXML
    private TextField lonMinField;
    @FXML
    private Button importButton;
    @FXML
    private TextField fileTextField;
    @FXML
    private Button browseButton;
    @FXML
    private TableView<MapRegion> mapTable;
    @FXML
    private TableColumn<MapRegion, String> nameColumn;
    @FXML
    private TableColumn<MapRegion, String> regionColumn;
    @FXML
    private TableColumn<MapRegion, Double> latMaxColumn;
    @FXML
    private TableColumn<MapRegion, Double> latMinColumn;
    @FXML
    private TableColumn<MapRegion, Double> lonMaxColumn;
    @FXML
    private TableColumn<MapRegion, Double> lonMinColumn;
    
    private final SportActivityApp app = SportActivityApp.getInstance();

    private final ObservableList<MapRegion> mapaList =
            FXCollections.observableArrayList();
    private Button backButton;

    // Establece las columnas que se han de rellenar para añadir un mapa
    // Carga los mapas de la bdd
    // Escucha si se puede dejar de deshabilitar el botón de delete
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name"));
        regionColumn.setCellValueFactory(
                new PropertyValueFactory<>("imagePath"));
        latMaxColumn.setCellValueFactory(
                new PropertyValueFactory<>("latMax"));
        latMinColumn.setCellValueFactory(
                new PropertyValueFactory<>("latMin"));
        lonMaxColumn.setCellValueFactory(
                new PropertyValueFactory<>("lonMax"));
        lonMinColumn.setCellValueFactory(
                new PropertyValueFactory<>("lonMin"));

        
        mapTable.setItems(mapaList);
        cargarMapas();
        
        formGrid.setDisable(true);
        
        deleteButton.setDisable(true);
        mapTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> deleteButton.setDisable(newVal == null)
        );

    }   

    //Carga la lista de los mapas de la bdd
    private void cargarMapas() {
        mapaList.clear();
        List<MapRegion> regiones = app.getMapRegions();
        if (regiones != null) {
            mapaList.addAll(regiones);
        }
    }

    // Acción de añadir mapa
    @FXML
    private void ddAction(ActionEvent event) {
        formGrid.setDisable(false);
        fileTextField.requestFocus();

    }

    // Borrar mapa tanto de la lista como de la bdd
    @FXML
    private void deleteAction(ActionEvent event) {
        MapRegion seleccionado = mapTable.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION,
            "¿Eliminar el mapa \"" + seleccionado.getName() + "\"?",
            ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                app.removeMapRegion(seleccionado);
                cargarMapas();
                if (mapaList.contains(seleccionado)) {
                    mostrarError("No se puede eliminar.\n"
                               + "El mapa está en uso por alguna actividad\n"
                               + "o es un mapa por defecto.");
                }
            }
        });
    }

    // Importar el mapa a la bdd con todos sus parámetros establecidos por el usuario
    @FXML
    private void importAction(ActionEvent event) {
        String rutaFichero = fileTextField.getText().trim();
        if (rutaFichero.isEmpty()) {
            mostrarError("Selecciona un fichero de imagen.");
            return;
        }
        File imageFile = new File(rutaFichero);
        if (!imageFile.exists()) {
            mostrarError("El fichero no existe.");
            return;
        }

        double latMax, latMin, lonMax, lonMin;
        try {
            latMax = Double.parseDouble(latMaxField.getText().trim());
            latMin = Double.parseDouble(latMinField.getText().trim());
            lonMax = Double.parseDouble(lonMaxField.getText().trim());
            lonMin = Double.parseDouble(lonMinField.getText().trim());
        } catch (NumberFormatException e) {
            mostrarError("Las coordenadas deben ser números válidos.\n"
                       + "Usa punto decimal (ej: 39.554883).");
            return;
        }

        if (latMax <= latMin) {
            mostrarError("La latitud máxima debe ser mayor que la mínima.");
            return;
        }
        if (lonMax <= lonMin) {
            mostrarError("La longitud máxima debe ser mayor que la mínima.");
            return;
        }

        String nombre = imageFile.getName();
        int punto = nombre.lastIndexOf('.');
        if (punto > 0) nombre = nombre.substring(0, punto);

        MapRegion nueva = app.addMapRegion(
                nombre, imageFile, latMin, latMax, lonMin, lonMax);

        if (nueva != null) {
            cargarMapas();
            limpiarFormulario();
            formGrid.setDisable(true); // volver a deshabilitar el formulario
            mostrarInfo("Mapa \"" + nueva.getName() + "\" importado correctamente.");
        } else {
            mostrarError("No se pudo importar el mapa.\n"
                       + "Comprueba que el fichero es válido.");
        }

    }

    // Elegir fichero de imagen
    @FXML
    private void browseAction(ActionEvent event) {
               FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imagen de mapa");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes JPG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Todos los ficheros", "*.*")
        );
        File f = fc.showOpenDialog(browseButton.getScene().getWindow());
        if (f != null) {
            fileTextField.setText(f.getAbsolutePath());
        } 
    }
    
    // Limpia los valores
    private void limpiarFormulario() {
        fileTextField.clear();
        latMaxField.clear();
        latMinField.clear();
        lonMaxField.clear();
        lonMinField.clear();
    }

    // Muestra el error
    private void mostrarError(String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
    
}
