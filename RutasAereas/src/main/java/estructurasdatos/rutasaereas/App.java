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

public class App extends Application {

    @Override
    public void start(Stage ventana) throws IOException {
        CargadorGrafo cargador = new CargadorGrafo();

        Grafo grafo = cargador.cargar(
                Path.of("datos", "aeropuertos.txt"),
                Path.of("datos", "rutas.txt"));

        FXMLLoader cargadorVista = new FXMLLoader(
                App.class.getResource("vista/principal.fxml"));

        Parent raiz = cargadorVista.load();

        PrincipalController controlador = cargadorVista.getController();
        controlador.setGrafo(grafo);

        Scene escena = new Scene(raiz, 640, 480);

        ventana.setTitle("Rutas aéreas");
        ventana.setScene(escena);
        ventana.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
