package estructurasdatos.rutasaereas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.persistencia.CargadorGrafo;
import java.nio.file.Path;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        CargadorGrafo cargador = new CargadorGrafo();

        Grafo grafo = cargador.cargar(
                Path.of("datos", "aeropuertos.txt"),
                Path.of("datos", "rutas.txt"));

        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("primary.fxml"));

        Parent root = loader.load();

        PrimaryController controller = loader.getController();
        controller.setGrafo(grafo);

        scene = new Scene(root, 640, 480);
        stage.setTitle("Rutas aéreas");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
