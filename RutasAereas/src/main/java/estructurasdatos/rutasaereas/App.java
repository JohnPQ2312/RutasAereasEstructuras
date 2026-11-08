package estructurasdatos.rutasaereas;

import estructurasdatos.rutasaereas.controlador.PrincipalController;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.persistencia.CargadorGrafo;
import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Inicia JavaFX, carga la red y entrega el mismo grafo al controlador de la ventana.
public class App extends Application {

    @Override
    public void start(Stage ventana) throws IOException {
        CargadorGrafo cargador = new CargadorGrafo();

        // Los archivos de datos se buscan desde la carpeta de ejecución del programa.
        Grafo grafo = cargador.cargar(
                Path.of("datos", "aeropuertos.txt"),
                Path.of("datos", "rutas.txt"));

        // La vista, a diferencia de los datos externos, forma parte de los recursos del programa.
        FXMLLoader cargadorVista = new FXMLLoader(
                App.class.getResource("vista/principal.fxml"));

        Parent raiz = cargadorVista.load();

        // load ya creó los controles y ejecutó initialize; ahora se puede conectar el modelo.
        PrincipalController controlador = cargadorVista.getController();
        controlador.setGrafo(grafo);

        Scene escena = new Scene(raiz, 1100, 720);

        ventana.setTitle("Rutas aéreas");
        ventana.setMinWidth(900);
        ventana.setMinHeight(650);
        ventana.setScene(escena);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
