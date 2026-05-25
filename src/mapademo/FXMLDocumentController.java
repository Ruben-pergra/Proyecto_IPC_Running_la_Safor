/*
 * ============================================================
 *  PROYECTO EJEMPLO – IPC 2026
 *  Asignatura: Interfaces Persona-Computador
 *  Universitat Politècnica de València
 * ============================================================
 *
 *  DESCRIPCIÓN GENERAL
 *  -------------------
 *  Este controlador gestiona la vista principal de la aplicación
 *  de puntos de interés (POI) sobre un mapa.
 *
 *  Funcionalidades implementadas:
 *   1. Carga y visualización de una imagen de mapa.
 *   2. Zoom interactivo mediante un Slider.
 *   3. Añadir POIs (texto) y anotaciones (círculos) con clic derecho.
 *   4. Listado de POIs en un ListView con CellFactory personalizada.
 *   5. Centrado animado del mapa al seleccionar un POI de la lista.
 *   6. Modo inserción: activar con botón y colocar POI con siguiente clic.
 *
 *  PATRÓN UTILIZADO: MVC (Model-View-Controller)
 *   - Modelo : clase Poi  (datos del punto de interés)
 *   - Vista  : FXMLDocument.fxml  (layout declarativo)
 *   - Control: esta clase (lógica de interacción)
 *
 * ============================================================
 */
package mapademo;

import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;

/**
 * Controlador principal de la aplicación de mapa con POIs.
 *
 * La anotación @FXML conecta automáticamente los campos de esta clase
 * con los elementos declarados en el fichero FXML mediante su atributo fx:id.
 *
 * Implementa {@link Initializable} para poder ejecutar código de
 * inicialización una vez que el FXML ha sido cargado completamente.
 */
public class FXMLDocumentController implements Initializable {

    // =========================================================
    //  ESTRUCTURA DE NODOS PARA ZOOM
    // =========================================================
    //
    //  El zoom se consigue escalando un Group (zoomGroup).
    //  Escalar un Group NO desplaza los nodos que contiene,
    //  lo que evita el "salto" visual al hacer zoom.
    //
    //  Jerarquía de nodos:
    //
    //  ScrollPane (map_scrollpane)
    //   └─ contentGroup          ← Group raíz dentro del ScrollPane
    //       └─ zoomGroup         ← se escala para el zoom
    //           └─ mapPane       ← Pane con la imagen y los POIs
    //               ├─ ImageView ← imagen del mapa
    //               ├─ Text      ← etiquetas de POIs
    //               └─ Circle    ← anotaciones circulares
    //
    // =========================================================

    /** Group que se escala para aplicar el zoom. */
    private Group zoomGroup;

    /**
     * Pane que actúa como lienzo del mapa.
     * Contiene la imagen de fondo y todos los elementos superpuestos
     * (textos, círculos, etc.). Sus dimensiones coinciden con las de
     * la imagen cargada.
     */
    private Pane mapPane;

    
    /** Menú contextual reutilizable para el clic derecho sobre el mapa. */
    private ContextMenu mapContextMenu;


    /**
     * Indica si el controlador está en modo inserción de POI.
     * {@code true} → el próximo clic izquierdo sobre el mapa abre el diálogo.
     */
    private boolean insertionMode = false;

    // =========================================================
    //  ELEMENTOS FXML  (inyectados automáticamente por el cargador)
    // =========================================================

    /** Lista lateral que muestra todos los POIs añadidos al mapa. */
    @FXML
    private ListView<Annotation> map_listview;

    /** ScrollPane que envuelve el mapa y permite desplazarlo. */
    @FXML
    private ScrollPane map_scrollpane;

    /**
     * Slider de zoom.
     * Rango: [0.5 – 1.5]. Valor inicial: 1.0 (sin zoom).
     * Cada cambio de valor llama al método zoom().
     */
    @FXML
    private Slider zoom_slider;

    /**
     * Botón de pin visible sobre el mapa.
     * Se desplaza hasta la posición del POI seleccionado en la lista.
     */
    private MenuButton map_pin;

    // FIX 5 — Eliminadas las variables sin uso:
    //   · 'mousePosistion' (errata + duplicado de mousePosition)
    //   · 'pin_info'       (inyectada pero nunca actualizada)

    /** Etiqueta en la barra de estado que muestra las coordenadas del ratón. */
    @FXML
    private Label mousePosition;
    @FXML
    private SplitPane splitPane;
 
    
    // =========================================================
    //  VARIABLES DE LOS ALUMNOS
    // =========================================================

    private Pane paneVistas;
    
    @FXML
    private ListView<MapRegion> mapa_listview;
    @FXML
    private VBox boxVistas;
    @FXML
    private Button bImportarGpx;
    @FXML
    private Button bBorrarGpx;
    
    @FXML
    private VBox boxAct;
    @FXML
    private ImageView homeButton;
    @FXML
    private Text lblDistanciaTotal;
    @FXML
    private Text lblDuracion;
    @FXML
    private Text lblVelocidadMax;
    @FXML
    private Text lblRitmoMedio;
    @FXML
    private Text lblDesnivelPos;
    @FXML
    private Text lblDesnivelNeg;
    @FXML
    private Text lblAltitudMax;
    @FXML
    private Text lblAltitudMin;
    @FXML
    private LineChart<Double, Double> perfilDesnivel;
    @FXML
    private ListView<Activity> actividades_listview;
    
    private java.util.List<javafx.scene.Node> hijosDefaultVistas;
    private SportActivityApp app = SportActivityApp.getInstance();
    
    @FXML
    private LineChart<Number, Number> elevationChart;
    @FXML 
    private NumberAxis xAxis;
    @FXML 
    private NumberAxis yAxis;
    private MapProjection currentProjection;
    private Circle hoverMarker;
    
    private Activity actividadActual;
    private final Map<javafx.scene.Node, Annotation> nodosAnotacion = new HashMap<>();
    
    // =========================================================
    //  MANEJADORES DE ZOOM
    // =========================================================

    /**
     * Aumenta el zoom en 0.1 unidades al pulsar el botón "+".
     *
     * @param event evento de acción del botón
     */
    @FXML
    void zoomIn(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal + 0.1);
    }

    /**
     * Reduce el zoom en 0.1 unidades al pulsar el botón "–".
     *
     * @param event evento de acción del botón
     */
    @FXML
    void zoomOut(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal - 0.1);
    }

    /**
     * Aplica el factor de escala al {@code zoomGroup}.
     *
     * Este método es invocado automáticamente cada vez que cambia el
     * valor del slider, gracias al listener registrado en {@link #initialize}.
     *
     * Truco: guardamos y restauramos los valores de scroll para que el
     * contenido visible no salte al cambiar la escala.
     *
     * @param scaleValue nuevo factor de escala (p. ej. 1.2 → 120 %)
     */
    private void zoom(double scaleValue) {
        // Guardamos la posición del scroll antes de escalar
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        // Aplicamos el zoom escalando el Group en ambos ejes
        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        // Restauramos la posición del scroll para que el centro visual
        // permanezca estable durante el zoom
        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    // =========================================================
    //  SELECCIÓN EN EL LISTVIEW → CENTRADO EN EL MAPA
    // =========================================================

    /**
     * Se ejecuta cuando el usuario hace clic en un elemento del ListView.
     *
     * Objetivo: centrar el ScrollPane sobre la posición del POI seleccionado
     * con una animación suave de 500 ms, y mover el pin al punto.
     *
     * Cálculo del scroll
     * ------------------
     * El ScrollPane expresa su posición como valores normalizados [0, 1]:
     *   · hValue = 0 → extremo izquierdo
     *   · hValue = 1 → extremo derecho
     *
     * Para centrar el POI necesitamos:
     *
     *   scrollH = (poiX_escalado - viewportAncho / 2)
     *             ─────────────────────────────────────
     *             (mapaAncho_escalado - viewportAncho)
     *
     * Aplicamos clamp para no salir del rango [0, 1].
     *
     * @param event evento de ratón sobre el ListView
     */

    // =========================================================
    //  CONSTRUCCIÓN DEL MAPA
    // =========================================================

    /**
     * Carga una imagen y construye la jerarquía de nodos del mapa.
     *
     * Este método puede llamarse varias veces (p. ej. al cambiar el mapa),
     * ya que sustituye completamente el contenido del ScrollPane.
     *
     * @param imgFile fichero de imagen a cargar como fondo del mapa
     */
    private void buildMap(File imgFile) {
        // Comprobación defensiva: si el fichero no existe mostramos un aviso
        if (!imgFile.exists()) {
            map_scrollpane.setContent(
                new Label("Imagen no encontrada: " + imgFile.getPath()));
            return;
        }

        // Cargamos la imagen y obtenemos sus dimensiones reales en píxeles
        Image img = new Image(imgFile.toURI().toString());
        double W = img.getWidth();
        double H = img.getHeight();

        // ── mapPane: lienzo del mapa ───────────────────────────────────
        // Usamos un Pane (y no un Group) para poder posicionar los nodos
        // hijos con coordenadas absolutas (setLayoutX / setLayoutY).
        mapPane = new Pane();
        mapPane.setPrefSize(W, H); // tamaño preferido = tamaño de la imagen
        mapPane.setMinSize(W, H);  // impedimos que el layout lo encoja
        mapPane.setMaxSize(W, H);  // impedimos que el layout lo agrande

        // Añadimos la imagen como fondo del Pane
        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        mapPane.getChildren().add(iv);

        // ── Manejador de clics sobre el mapa ──────────────────────────
        // Gestionamos el clic derecho (menú contextual) y el clic izquierdo
        // en modo inserción (FIX 2).
        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Clic derecho → mostrar menú contextual
                onMapRightClick(e.getX(), e.getY());

            } else if (e.getButton() == MouseButton.PRIMARY && insertionMode) {
                // FIX 2: clic izquierdo en modo inserción → añadir POI y desactivar modo
                insertionMode = false;
                mapPane.setStyle(""); // Restauramos el cursor normal
            }
        });

        // ── Jerarquía de Groups para el zoom ──────────────────────────
        // contentGroup es el nodo raíz que recibe el ScrollPane.
        // zoomGroup es el que se escala; anidar un Group dentro de otro
        // evita que el ScrollPane reajuste su contenido durante el escalado.
        zoomGroup = new Group();
        Group contentGroup = new Group();
        zoomGroup.getChildren().add(mapPane);
        contentGroup.getChildren().add(zoomGroup);

        // Aplicamos el zoom actual (valor actual del slider)
        double zoom = zoom_slider.getValue();
        zoomGroup.setScaleX(zoom);
        zoomGroup.setScaleY(zoom);

        // Asignamos el contentGroup como contenido del ScrollPane
        map_scrollpane.setContent(contentGroup);
        
        // Cargamos las propiedades de HoverMarker
        setupHoverMarker();
    }

    // =========================================================
    //  MENÚ CONTEXTUAL (clic derecho sobre el mapa)
    // =========================================================

    /**
     * Muestra el menú contextual reutilizable en la posición del clic.
     *
     * Las acciones de los MenuItem se actualizan con las coordenadas
     * del clic actual antes de mostrar el menú.
     *
     * @param x coordenada X del clic en el sistema local del mapPane
     * @param y coordenada Y del clic en el sistema local del mapPane
     */
    private void onMapRightClick(double x, double y) {
        // FIX 6: cerramos el menú si ya estaba visible (evita instancias flotantes)
        mapContextMenu.hide();

        // Actualizamos las acciones de los items con las coordenadas actuales.
        // Usamos variables final para que el lambda pueda capturarlas.
        final double clickX = x;
        final double clickY = y;
        mapContextMenu.getItems().get(0).setOnAction(e -> abrirDialogoAnotacion(AnnotationType.POINT,  clickX, clickY));
        mapContextMenu.getItems().get(1).setOnAction(e -> abrirDialogoAnotacion(AnnotationType.TEXT,   clickX, clickY));
        mapContextMenu.getItems().get(2).setOnAction(e -> abrirDialogoAnotacion(AnnotationType.LINE,   clickX, clickY));
        mapContextMenu.getItems().get(3).setOnAction(e -> abrirDialogoAnotacion(AnnotationType.CIRCLE, clickX, clickY));

        // Mostramos el menú en coordenadas de pantalla
        mapContextMenu.show(
            mapPane.getScene().getWindow(),
            mapPane.localToScreen(x, y).getX(),
            mapPane.localToScreen(x, y).getY()
        );
       
    }

    // =========================================================
    //  INICIALIZACIÓN DEL CONTROLADOR
    // =========================================================

    /**
     * Método llamado automáticamente por el FXMLLoader tras inyectar
     * todos los elementos {@code @FXML}.
     *
     * Aquí configuramos:
     *  - El slider de zoom y su listener.
     *  - El ContextMenu reutilizable (FIX 6).
     *  - La CellFactory del ListView (FIX 4).
     *  - La carga del mapa inicial.
     *
     * @param url  URL del documento FXML (no usado aquí)
     * @param rb   paquete de recursos de internacionalización (no usado aquí)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hijosDefaultVistas = new java.util.ArrayList<>(boxVistas.getChildren());

        // ── Configuración del slider de zoom ──────────────────────────
        zoom_slider.setMin(0.5);   // zoom mínimo: 50 %
        zoom_slider.setMax(1.5);   // zoom máximo: 150 %
        zoom_slider.setValue(1.0); // valor inicial: 100 %

        // Listener que invoca zoom() cada vez que el slider cambia de valor.
        // Usamos una expresión lambda en lugar de una clase anónima por brevedad.
        zoom_slider.valueProperty().addListener(
            (observable, oldVal, newVal) -> zoom((Double) newVal)
        );

        // Establecemos los items de las anotaciones
        MenuItem miPoint  = new MenuItem("📍 Añadir punto");
        MenuItem miText   = new MenuItem("📝 Añadir texto");
        MenuItem miLine   = new MenuItem("📏 Añadir línea");
        MenuItem miCircle = new MenuItem("⭕ Añadir círculo");
        mapContextMenu = new ContextMenu(miPoint, miText, miLine, miCircle);

        
        // =========================================================
        //  CODIGO DE LOS ALUMNOS
        // =========================================================
        
        // Listener de la  listview de las anotaciones, actualiza los items en la lista asociada
        map_listview.setCellFactory(listView -> new ListCell<Annotation>() {
            @Override
            protected void updateItem(Annotation ann, boolean empty) {
                super.updateItem(ann, empty);
                if (empty || ann == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String icono = switch (ann.getType()) {
                        case POINT  -> "📍"; case TEXT   -> "📝"; case LINE   -> "📏"; case CIRCLE -> "⭕";
                    };
                    String texto = ann.getText().isEmpty() ? "(sin texto)" : ann.getText();
                    setText(icono + " " + ann.getType().name() + " – " + texto);
                }
            }
        });
        
        // Cargamos la lista de todos los mapas de la bdd
        cargarListaMapas();
        
        // Listener para manejar la listview de las actividades
        /*Este Listener ha de limpiar tanto anotaciones, como actividades, etc de la vista del mapa
        Como iniciar la gráfica de elevación    
        Como cargar las anotaciones tras limpiar las viejas
        Como dibujar la ruta de la actividad
        Como de cargar las estadísticas de la ruta
        */
        actividades_listview.getSelectionModel().selectedItemProperty().addListener((obs, oldAct, newAct) -> {
            if (newAct != null && mapa_listview.getSelectionModel().getSelectedItem() != null) {
                
                this.actividadActual = newAct;
                limpiarMapaCompleto();
                
                cargarEstadisticas(newAct);
                
                this.currentProjection = new MapProjection(mapa_listview.getSelectionModel().getSelectedItem(), mapPane.getWidth(), mapPane.getHeight());
                setupHoverMarker();
                loadElevationChart(newAct);
                dibujarRuta(newAct, currentProjection);
                
                cargarAnotacionesDeActividad(newAct);
            }
        });
        
        //Listener que se encarga de gestionar la lista de mapas
        /*
        Este listener actualiza los datos pertinentes y dibuja en pantalla el mapa clickado
        */
        mapa_listview.getSelectionModel().selectedItemProperty().addListener((observable, oldMap, newMap) -> {
            if (newMap != null) {
                File archivoImagen = new File(newMap.getImagePath());
                buildMap(archivoImagen);
                
                this.actividadActual = null;
                limpiarEstadisticas();
                
                cargarActividadesDelMapa(newMap);
            }
        });
    }

    // =========================================================
    //  MÉTODOS DEL CONTROLADOR
    // =========================================================

    /**
     * Actualiza la etiqueta {@code mousePosition} con las coordenadas
     * actuales del ratón, tanto en el sistema de la escena como en el
     * sistema local del nodo sobre el que se mueve.
     *
     * Útil para depuración y para que los alumnos comprendan la diferencia
     * entre coordenadas de escena y coordenadas locales.
     *
     * @param event evento de movimiento del ratón
     */
    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText(
            "sceneX: " + (int) event.getSceneX() +
            ", sceneY: " + (int) event.getSceneY() + "\n" +
            "         X: " + (int) event.getX() +
            ",          Y: " + (int) event.getY()
        );
    }

    /**
     * Muestra un diálogo informativo con datos de la asignatura.
     *
     * Nota: accedemos al Stage del diálogo para poder personalizar
     * su icono, ya que Alert no expone directamente esa propiedad.
     *
     * @param event evento de acción del menú
     */
    @FXML
    private void about(ActionEvent event) {
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION);

        // Personalizamos el icono de la ventana del diálogo
        Stage dialogStage = (Stage) mensaje.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
            new Image(getClass().getResourceAsStream("/resources/logo.png"))
        );

        mensaje.setTitle("Acerca de");
        mensaje.setHeaderText("IPC - 2026");
        mensaje.showAndWait(); // Bloquea hasta que el usuario cierra el diálogo
    }

    /**
     * Abre un selector de fichero para que el usuario elija una imagen
     * diferente como mapa y reconstruye toda la vista.
     *
     * FIX 3: se comprueba que imgFile no sea null antes de usarlo,
     * evitando NullPointerException cuando el usuario cierra el FileChooser
     * sin seleccionar ningún fichero.
     *
     * @param event evento de acción del menú
     * @throws IOException si hay un problema al obtener la ruta canónica
     */
    @FXML
    private void cambiarMapa(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(".")); // Empezamos en el directorio del proyecto

        File imgFile = fc.showOpenDialog(zoom_slider.getScene().getWindow());

        // FIX 3: showOpenDialog() devuelve null si el usuario cancela la selección
        if (imgFile != null) {
            System.out.println("Mapa seleccionado: " + imgFile.getCanonicalPath());
            buildMap(imgFile); // Reconstruimos la vista con la nueva imagen
            map_listview.getItems().clear(); // Borramos los datos del mapa anterior
        }
    }

    /**
     * Dibuja un círculo rojo de radio 10 px en la posición indicada.
     *
     * Ejemplo sencillo de cómo añadir formas vectoriales (Shape) sobre el mapa.
     * Los alumnos pueden extenderlo para:
     *  - Elegir color dinámicamente.
     *  - Asociar información al círculo (tooltip, popup, etc.).
     *  - Permitir moverlo con arrastrar y soltar (drag and drop).
     *
     * @param x coordenada X en el sistema local del mapPane
     * @param y coordenada Y en el sistema local del mapPane
     */
    private void addCircle(double x, double y) {
        Circle circle = new Circle(10, Color.RED); // radio = 10 px, color = rojo
        circle.setCenterX(x);
        circle.setCenterY(y);
        mapPane.getChildren().add(circle); // Se añade sobre el mapa como cualquier nodo
    }

    // =========================================================
    //  METODOS DE LOS ALUMNOS
    // =========================================================
    
    // Función para darle acción al EditarPErfil
    @FXML
    private void OnEditarPerfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLEditarPerfil.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Editar Perfil - Running la Safor");

            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar el formulario de registro: " + e.getMessage());
        }
    }
    
    // Mostrar en el botón de ayuda las distintas opciones de la aplicación
    @FXML
    private void mostrarAyuda(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        
        alerta.setTitle("Ayuda general - Running la Safor");
        alerta.setHeaderText("Funciones de la aplicacion"); 
        alerta.setContentText("• Puedes elegir entre los diferentes mapas y se mostrarán tus actividades \n"
                            + "• En la pestaña mapas puedes añadir más o borrarlos \n"
                            + "• Con click derecho sobre el mapa puedes hacer anotaciones \n"
                            + "• En el apartado de usuario puedes cerrar sesion o modificar perfil \n"
                            + "• Moviendo el ratón por la línea de desnivel (abajo de la imagen del mapa) puedes ver el trazado de la actividad \n"
                            + "• El botón home (el icono de la casa), te sirve para retornar del apartado de sesiones al menú principal \n");

        alerta.showAndWait();
    }
    
    // Botón para cerrar la sesión y guardar las estadísticas de esta misma, te lleva a la parte de inicio
    @FXML
    private void OnCerrarSesion(ActionEvent event) {
        try {
            mousePosition.getScene().getWindow().hide();
            app.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLInicio.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Página de inicio - Running la Safor");
            stage.show();

        } catch (Exception e) {
            System.err.println("Error al intentar volver a la pantalla de inicio:");
            e.printStackTrace();
        }
    }

    //Botón q te lleva a la sección de añadir mapa
    @FXML
    private void onMapsButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mapademo/FXMLMapa.fxml"));
            Parent vistaMapas = loader.load();
            splitPane.getScene().setRoot(vistaMapas);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo cargar la vista de mapas.");
            alert.showAndWait();
        }
    }
    
    //Botón q te lleva a la sección de sesiones
    @FXML
    private void onClickSesiones(ActionEvent event) {

        try {
            limpiarEstadisticas();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("Sesiones.fxml")
            );
            javafx.scene.Node vista = loader.load();
            SesionesController controller = loader.getController();
            controller.onEnter();

            boxVistas.getChildren().clear();
            boxVistas.getChildren().add(vista);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Carga de estadísticas de la actividad en la tabla
     private void cargarEstadisticas(Activity activity) {
        lblDistanciaTotal.setText(
                String.format("%.2f km", activity.getTotalDistance() / 1000.0));
        lblDuracion.setText(
                Utils.formatDuration(activity.getDuration()));
        lblVelocidadMax.setText(
                String.format("%.1f km/h", activity.getAverageSpeed()));
        lblRitmoMedio.setText(
                String.format("%.1f min/km", activity.getAveragePace()));
        lblDesnivelPos.setText(
                String.format("+%.0f m", activity.getElevationGain()));
        lblDesnivelNeg.setText(
                String.format("-%.0f m", activity.getElevationLoss()));
        lblAltitudMax.setText(
                String.format("%.0f m", activity.getMaxElevation()));
        lblAltitudMin.setText(
                String.format("%.0f m", activity.getMinElevation()));
    }
    
    // Limpia de las estadísticas de la tabla
    private void limpiarEstadisticas() {
        lblDistanciaTotal.setText("- km");
        lblDuracion.setText("-");
        lblVelocidadMax.setText("- km/h");
        lblRitmoMedio.setText("- min/km");
        lblDesnivelPos.setText("- m");
        lblDesnivelNeg.setText("- m");
        lblAltitudMax.setText("- m");
        lblAltitudMin.setText("- m");
    }
    
    // Botón para importar gpx
    /*
    Este botón se encarga de importarlo y asociarlo al mapa en la bdd
    La carga en la listview de actividades
    Además al importarla la dibuja
    */
    @FXML
    private void onImportarGpx(ActionEvent event) {
        mostrarHome();

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar fichero GPX");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Ficheros GPX", "*.gpx")
        );
        File gpxFile = fc.showOpenDialog(bImportarGpx.getScene().getWindow());

        if (gpxFile != null) {
            Activity activity = app.importActivity(gpxFile);
            
            if (activity != null) {
                MapRegion regionSugerida = activity.getSuggestedMap();
                mapa_listview.getSelectionModel().select(regionSugerida);
                
                cargarActividadesDelMapa(regionSugerida);
                
                actividades_listview.getSelectionModel().select(activity);
                
                Alert a = new Alert(Alert.AlertType.INFORMATION, "¡Actividad importada y guardada con éxito!");
                a.showAndWait();
                setupHoverMarker();             
                loadElevationChart(activity); 
            }
        }
    }
    
    // Mostrar el botón de home
    private void mostrarHome() {
        boxVistas.getChildren().clear();
        for (javafx.scene.Node n : hijosDefaultVistas) {
            boxVistas.getChildren().add(n);
        }
    }

    
    // Botón para borrar el gpx
    /*
    Este la borra tanto de la listview como de la bdd y la desasocia del mapa
    Limpia del mapa todas las propiedades asociadas y las estadísticas de la tabla
    */
    @FXML
    private void onBorrarGpx(ActionEvent event) {
        Activity actividadSeleccionada = actividades_listview.getSelectionModel().getSelectedItem();
        
        if (actividadSeleccionada == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Atención");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, selecciona una actividad de la lista para borrarla.");
            alerta.showAndWait();
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar borrado");
        confirmacion.setHeaderText("¿Estás seguro de que deseas borrar esta actividad?");
        confirmacion.setContentText("Esta acción eliminará la actividad de tu base de datos y no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            
            app.removeActivity(actividadSeleccionada);
            this.actividadActual = null;
            
            MapRegion mapaActual = mapa_listview.getSelectionModel().getSelectedItem();
            if (mapaActual != null) {
                cargarActividadesDelMapa(mapaActual);
            }
            
            limpiarMapaCompleto(); 
            limpiarEstadisticas();
            elevationChart.getData().clear();
            
            Alert exito = new Alert(Alert.AlertType.INFORMATION);
            exito.setTitle("Éxito");
            exito.setHeaderText(null);
            exito.setContentText("La actividad se ha borrado correctamente.");
            exito.showAndWait();
        }
    }
    
    // Dibuja la ruta de la actividad
    private void dibujarRuta(Activity activity, MapProjection mapa) {
        javafx.scene.shape.Polyline ruta = new javafx.scene.shape.Polyline();
        ruta.setStroke(Color.BLUE);
        ruta.setStrokeWidth(2);

        for (TrackPoint tp : activity.getTrackPoints()) {
            Point2D p = mapa.project(tp);
            ruta.getPoints().addAll(p.getX(), p.getY());
        }

        mapPane.getChildren().add(ruta);

        Point2D pInicio = mapa.project(activity.getStartPoint());
        Circle inicio = new Circle(8, Color.GREEN);
        inicio.setCenterX(pInicio.getX());
        inicio.setCenterY(pInicio.getY());

        Point2D pFin = mapa.project(activity.getEndPoint());
        Circle fin = new Circle(8, Color.RED);
        fin.setCenterX(pFin.getX());
        fin.setCenterY(pFin.getY());

        Platform.runLater(() -> {
            map_scrollpane.setHvalue(pInicio.getX() / mapPane.getWidth());
            map_scrollpane.setVvalue(pInicio.getY() / mapPane.getHeight());

            mapPane.getChildren().addAll(inicio, fin);
        });
    }

    // Carga la lista de mapas de la bdd y actualiza el item
    private void cargarListaMapas() {
        List<MapRegion> regiones = app.getMapRegions();
        if (regiones != null) {
            mapa_listview.setItems(FXCollections.observableArrayList(regiones));
        }

        mapa_listview.setCellFactory(lv -> new ListCell<MapRegion>() {
            @Override
            protected void updateItem(MapRegion item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }
    
    // Botón para volver a la parte principal
    @FXML
    private void onHome(MouseEvent event) {
        mostrarHome();
    }
    
    // Cargamos las actividades en el mapa, para ello vacíamos la lista primero, luego la rellenamos con las q hay y cargamos solo las asociadas a ese mapa
    private void cargarActividadesDelMapa(MapRegion mapa) {
        actividades_listview.getItems().clear();
        
        List<Activity> misActividades = app.getUserActivities();
        
        if (misActividades != null) {
            for (Activity act : misActividades) {
                if (act.getSuggestedMap() != null && act.getSuggestedMap().getName().equals(mapa.getName())) {
                    actividades_listview.getItems().add(act);
                }
            }
        }
    }
    
    // SetUp de las propiedades del círculoq  se mueve con el desnivel
    private void setupHoverMarker() {
        if (hoverMarker != null) {
            mapPane.getChildren().remove(hoverMarker);
        }
        hoverMarker = new Circle(7);
        hoverMarker.setFill(Color.DODGERBLUE);
        hoverMarker.setStroke(Color.WHITE);
        hoverMarker.setStrokeWidth(2);
        hoverMarker.setVisible(false);
        hoverMarker.setMouseTransparent(true); // no interfiere con clics del mapa
        mapPane.getChildren().add(hoverMarker);
    }

    // Cargar el perfil de desnivel
    /*
    Obtenemos la tabla y miramos por dónde se mueve el ratón para cargar el punto en consecuencia en la ruta de la actividad
    */
    private void loadElevationChart(Activity activity) {
        elevationChart.getData().clear();

        javafx.scene.chart.XYChart.Series<Number, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Altitud");

        List<TrackPoint> points = activity.getTrackPoints();
        double distAcum = 0.0;

        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                distAcum += points.get(i).distanceTo(points.get(i - 1)) / 1000.0;
            }

            javafx.scene.chart.XYChart.Data<Number, Number> dato = new javafx.scene.chart.XYChart.Data<>(distAcum, points.get(i).getElevation());

            final int idx = i;
            dato.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setOnMouseEntered(e -> {
                        if (currentProjection == null || hoverMarker == null) return;
                        Point2D pixel = currentProjection.project(points.get(idx));
                        hoverMarker.setCenterX(pixel.getX());
                        hoverMarker.setCenterY(pixel.getY());
                        hoverMarker.setVisible(true);
                        hoverMarker.toFront();
                    });
                }
            });

            series.getData().add(dato);
        }

        elevationChart.getData().add(series);

        elevationChart.setOnMouseExited(e -> {
            if (hoverMarker != null) hoverMarker.setVisible(false);
        });
    }
    
    // Abrir el diálogo para poner anotaciones
    /*
    Puedes elegir color de la anotación q pones
    Tras establecer la anotación, la guarda asociada a la actividad
    */
    private void abrirDialogoAnotacion(AnnotationType tipo, double x, double y) {
        if (this.actividadActual == null || currentProjection == null) {
            Alert aviso = new Alert(Alert.AlertType.WARNING);
            aviso.setTitle("Sin actividad");
            aviso.setHeaderText(null);
            aviso.setContentText("Importa o selecciona una actividad antes de añadir anotaciones.");
            aviso.showAndWait();
            return;
        }

        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Nueva anotación");
        dialog.setHeaderText("Tipo: " + tipo.name());

        ButtonType okBtn = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        TextField textoField = new TextField();
        textoField.setPromptText("Texto de la anotación (opcional)");

        javafx.scene.control.ColorPicker colorPicker = new javafx.scene.control.ColorPicker(Color.RED);

        VBox contenido = new VBox(10,
            new Label("Texto:"), textoField,
            new Label("Color:"), colorPicker
        );
        dialog.getDialogPane().setContent(contenido);

        dialog.setResultConverter(btn -> {
            if (btn == okBtn) {
                Color c = colorPicker.getValue();
                String hex = String.format("#%02X%02X%02X",
                    (int)(c.getRed()   * 255),
                    (int)(c.getGreen() * 255),
                    (int)(c.getBlue()  * 255));
                return new String[]{ textoField.getText().trim(), hex };
            }
            return null;
        });

        Optional<String[]> resultado = dialog.showAndWait();
        if (resultado.isEmpty()) return;

        String texto = resultado.get()[0];
        String color = resultado.get()[1];

        GeoPoint geo = currentProjection.unproject(x, y);

    
        List<GeoPoint> puntos = (tipo == AnnotationType.LINE || tipo == AnnotationType.CIRCLE)
            ? List.of(geo, geo)
            : List.of(geo);

        Annotation ann = new Annotation(tipo, texto, color, 2.0, puntos);
        Annotation guardada = app.addAnnotation(this.actividadActual, ann);

        if (guardada != null) {
            dibujarAnotacion(guardada, x, y, color);
        }
    }
    
    // Dibujar anotación sobre el mapa
    private void dibujarAnotacion(Annotation ann, double x, double y, String colorHex) {
        Color color = Color.web(colorHex);

        switch (ann.getType()) {
            case POINT -> {
                Circle punto = new Circle(8, color);
                punto.setCenterX(x);
                punto.setCenterY(y);
                punto.setOpacity(0.85);
                registrarNodoAnotacion(punto, ann);
                mapPane.getChildren().add(punto);

                if (!ann.getText().isEmpty()) {
                    Text label = new Text(ann.getText());
                    label.setX(x + 10);
                    label.setY(y - 5);
                    label.setFill(color);
                    registrarNodoAnotacion(label, ann);
                    mapPane.getChildren().add(label);
                }
            }
            case TEXT -> {
                Text label = new Text(ann.getText().isEmpty() ? "Anotación" : ann.getText());
                label.setX(x);
                label.setY(y);
                label.setFill(color);
                label.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                registrarNodoAnotacion(label, ann);
                mapPane.getChildren().add(label);
            }
            case LINE -> {
                javafx.scene.shape.Line linea = new javafx.scene.shape.Line(x - 20, y, x + 20, y);
                linea.setStroke(color);
                linea.setStrokeWidth(2);
                registrarNodoAnotacion(linea, ann);
                mapPane.getChildren().add(linea);

                if (!ann.getText().isEmpty()) {
                    Text label = new Text(ann.getText());
                    label.setX(x + 5);
                    label.setY(y - 5);
                    label.setFill(color);
                    registrarNodoAnotacion(label, ann);
                    mapPane.getChildren().add(label);
                }
            }
            case CIRCLE -> {
                Circle circulo = new Circle(15);
                circulo.setCenterX(x);
                circulo.setCenterY(y);
                circulo.setFill(Color.TRANSPARENT);
                circulo.setStroke(color);
                circulo.setStrokeWidth(2);
                registrarNodoAnotacion(circulo, ann);
                mapPane.getChildren().add(circulo);

                if (!ann.getText().isEmpty()) {
                    Text label = new Text(ann.getText());
                    label.setX(x + 17);
                    label.setY(y);
                    label.setFill(color);
                    registrarNodoAnotacion(label, ann);
                    mapPane.getChildren().add(label);
                }
            }
        }
    }
    
    // Registra la anotación en la listview y gestiona la eliminación de estas
     private void registrarNodoAnotacion(javafx.scene.Node nodo, Annotation ann) {
        nodosAnotacion.put(nodo, ann);

        boolean yaEnLista = map_listview.getItems().stream().anyMatch(a -> a.getId() == ann.getId());
        if (!yaEnLista) {
            map_listview.getItems().add(ann);
        }

        MenuItem miEliminar = new MenuItem("🗑 Eliminar anotación");
        ContextMenu menuBorrar = new ContextMenu(miEliminar);

        miEliminar.setOnAction(e -> {
            app.removeAnnotation(ann);

            List<javafx.scene.Node> aEliminar = nodosAnotacion.entrySet().stream()
                .filter(entry -> entry.getValue().getId() == ann.getId())
                .map(Map.Entry::getKey)
                .toList();
            mapPane.getChildren().removeAll(aEliminar);
            aEliminar.forEach(nodosAnotacion::remove);

            map_listview.getItems().removeIf(a -> a.getId() == ann.getId());
        });

        nodo.setOnMouseClicked(ev -> {
            if (ev.getButton() == MouseButton.SECONDARY) {
                mapContextMenu.hide();
                menuBorrar.show(nodo, ev.getScreenX(), ev.getScreenY());
                ev.consume();
            }
        });
    }
     
    // Limpia las propiedades asociadas al mapa
    /*
    Elimina tanto anotaciones 
    Como rutas y marcadores de inicio
    Como los items del listview de anotaciones 
    */
    private void limpiarMapaCompleto() {
        mapPane.getChildren().removeAll(nodosAnotacion.keySet());
        nodosAnotacion.clear();

        mapPane.getChildren().removeIf(n ->
            n instanceof javafx.scene.shape.Polyline ||
            (n instanceof Circle && n != hoverMarker)
        );

        map_listview.getItems().clear();
    }
    
    // Carga las anotaciones de cada actividad en el mapa y las dibuja
    private void cargarAnotacionesDeActividad(Activity activity) {
        List<Annotation> anotaciones = activity.getAnnotations();
        if (anotaciones == null) return;

        for (Annotation ann : anotaciones) {
            if (ann.getGeoPoints() == null || ann.getGeoPoints().isEmpty()) continue;

            GeoPoint geo = ann.getGeoPoints().get(0);
            Point2D pixel = currentProjection.project(geo);

            dibujarAnotacion(ann, pixel.getX(), pixel.getY(), ann.getColor());
        }
    }
}