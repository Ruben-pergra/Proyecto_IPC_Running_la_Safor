/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

/**
 * FXML Controller class
 *
 * @author charlottediaz
 */
public class SesionesController implements Initializable {

    @FXML
    private Label lblTotalSesiones;
    @FXML
    private Label lblTiempoTotal;
    @FXML
    private Label lblTotalImportadas;
    @FXML
    private Label lblAnotacionesCreadas;
    @FXML
    private TableView<Session> tableView;
    @FXML
    private TableColumn<Session, String> colFecha;
    @FXML
    private TableColumn<Session, String> colDuracion;
    @FXML
    private TableColumn<Session, String> colImportadas;
    @FXML
    private TableColumn<Session, String> colVistas;
    @FXML
    private TableColumn<Session, String> colAnotaciones;
    @FXML
    private Label lblFooterTotal;
    @FXML
    private Label lblFooterDuracion;
    @FXML
    private Label lblFooterImportadas;
    @FXML
    private Label lblFooterVistas;
    @FXML
    private Label lblFooterAnotaciones;
    
    
    private int actividadesVistasActual = 0;

    public void setActividadesVistas(int n) {
        this.actividadesVistasActual = n;
    }

    /**
     * Initializes the controller class.
     */
    private SportActivityApp app = SportActivityApp.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        configurarColumnas();
        onEnter();

    }

    private void configurarColumnas() {

        colDuracion.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        Utils.formatDuration(cell.getValue().getDuration())
                )
        );

        colVistas.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        String.valueOf(cell.getValue().getViewedActivities())
                )
        );

        colAnotaciones.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        String.valueOf(cell.getValue().getAnnotationsCreated())
                )
        );

        colFecha.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        cell.getValue().getStartTime().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        )
                )
        );

        colImportadas.setCellValueFactory(cell
                -> new SimpleStringProperty(
                        String.valueOf(cell.getValue().getImportedActivities())
                )
        );
    }

    public void onEnter() {
        User user = app.getCurrentUser();
        if (user == null) {
            return;
        }

        List<Session> sessions = app.getSessionsByUser(user);
        ObservableList<Session> data
                = FXCollections.observableArrayList(sessions);
        
        
        if (!data.isEmpty()) {
            colVistas.setCellValueFactory(cell -> {
                Session s = cell.getValue();
                boolean esPrimera = data.indexOf(s) == 0;
                int vistas = esPrimera
                    ? (int) s.getViewedActivities() + actividadesVistasActual
                     : (int) s.getViewedActivities();
                return new SimpleStringProperty(String.valueOf(vistas));
            });
        }

        tableView.setItems(data);

        lblTotalSesiones.setText(String.valueOf(sessions.size()));

        long totalImp = sessions.stream().mapToLong(Session::getImportedActivities).sum();
        lblTotalImportadas.setText(String.valueOf(totalImp));

        long totalAnn = sessions.stream()
                .mapToLong(Session::getAnnotationsCreated).sum();
        lblAnotacionesCreadas.setText(String.valueOf(totalAnn));

        java.time.Duration totalDur = sessions.stream()
                .map(Session::getDuration)
                .reduce(java.time.Duration.ZERO, java.time.Duration::plus);
        lblTiempoTotal.setText(Utils.formatDuration(totalDur));

        lblFooterTotal.setText("Total (" + sessions.size() + " sesiones)");
        lblFooterDuracion.setText(Utils.formatDuration(totalDur));
        lblFooterImportadas.setText(String.valueOf(totalImp));

        long totalVis = sessions.stream().mapToLong(Session::getViewedActivities).sum();
        long totalVisConActual = totalVis + actividadesVistasActual;
        lblFooterVistas.setText(String.valueOf(totalVisConActual));
        lblFooterAnotaciones.setText(String.valueOf(totalAnn));
    }

}