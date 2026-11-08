package estructurasdatos.rutasaereas.controlador;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.modelo.Ruta;
import estructurasdatos.rutasaereas.algoritmos.BuscadorRutas;
import estructurasdatos.rutasaereas.modelo.ResultadoRecorrido;
import estructurasdatos.rutasaereas.algoritmos.CriterioPeso;
import estructurasdatos.rutasaereas.vista.MapaGrafo;
import estructurasdatos.rutasaereas.vista.AnimadorVuelo;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.control.ComboBox;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

// Recibe las acciones de la ventana y coordina búsquedas, resultados, mapa y animación.
// Las reglas del grafo y los algoritmos se mantienen en sus clases correspondientes.
public class PrincipalController {

    // FXMLLoader enlaza estos campos con los controles definidos en la vista.
    @FXML
    private Label estadoCarga;

    @FXML
    private TextField codigoAeropuerto;

    @FXML
    private TextArea resultadoConsulta;

    @FXML
    private TextField origenRecorrido;

    @FXML
    private TextField destinoRecorrido;

    @FXML
    private ComboBox<String> criterioRecorrido;

    @FXML
    private Pane contenedorMapa;

    @FXML
    private Canvas lienzoMapa;

    @FXML
    private Label estadoVuelo;

    private AnimadorVuelo animador;

    private MapaGrafo mapa;

    private BuscadorRutas buscador;

    private Grafo grafo;

    // Se ejecuta después de enlazar los controles, antes de recibir el grafo desde App.
    @FXML
    private void initialize() {
        criterioRecorrido.getItems().setAll(
                "Menos vuelos",
                "Menor tiempo",
                "Menor costo");

        criterioRecorrido.getSelectionModel().selectFirst();

        mapa = new MapaGrafo(lienzoMapa);

        // El Canvas sigue el tamaño disponible del contenedor al redimensionar la ventana.
        lienzoMapa.widthProperty().bind(contenedorMapa.widthProperty());
        lienzoMapa.heightProperty().bind(contenedorMapa.heightProperty());
    }

    // Buscador, mapa y animador comparten la misma red y ven sus cancelaciones en memoria.
    public void setGrafo(Grafo grafo) {
        // Detiene la secuencia anterior antes de reemplazar sus referencias al grafo.
        if (animador != null) {
            animador.detener();
        }

        this.grafo = grafo;
        this.buscador = new BuscadorRutas(grafo);

        mapa.setGrafo(grafo);
        animador = new AnimadorVuelo(grafo, mapa, estadoVuelo);

        actualizarEstadoCarga();
    }

    // Restablece la cámara; conserva la consulta y las conexiones activas.
    @FXML
    private void restablecerMapa() {
        mapa.restablecerVista();
    }

    // El contador de rutas corresponde a las conexiones que siguen activas.
    private void actualizarEstadoCarga() {
        estadoCarga.setText(
                "Aeropuertos: " + grafo.obtenerAeropuertos().size()
                + " | Rutas: " + grafo.contarRutas());
    }

    // Consulta los datos y las salidas directas de un aeropuerto, sin buscar itinerarios.
    @FXML
    private void consultarAeropuerto() {
        // Limpia la consulta anterior antes de validar para no dejar un viaje en pantalla.
        animador.detener();
        mapa.mostrarRecorrido(null);

        // Normaliza la entrada del usuario, sin cambiar los códigos cargados del archivo.
        String codigo = codigoAeropuerto.getText()
                .trim().toUpperCase(Locale.ROOT);

        if (codigo.isEmpty()) {
            resultadoConsulta.setText("Escribe el codigo de un aeropuerto.");
            return;
        }

        Aeropuerto aeropuerto = grafo.buscarAeropuerto(codigo);

        if (aeropuerto == null) {
            resultadoConsulta.setText("No existe el aeropuerto: " + codigo);
            return;
        }

        StringBuilder texto = new StringBuilder();

        // Los guiones bajos se sustituyen solo al mostrar ciudad y país.
        texto.append("Codigo: ").append(aeropuerto.getCodigo()).append('\n');
        texto.append("Ciudad: ")
                .append(aeropuerto.getCiudad().replace('_', ' ')).append('\n');
        texto.append("Pais: ")
                .append(aeropuerto.getPais().replace('_', ' ')).append('\n');
        texto.append("Posicion en el mapa: (")
                .append(aeropuerto.getX()).append(", ")
                .append(aeropuerto.getY()).append(")\n");

        List<Ruta> rutas = grafo.obtenerRutasSalientes(codigo);
        texto.append("\nRutas salientes:\n");

        if (rutas.isEmpty()) {
            texto.append("Este aeropuerto no tiene rutas salientes.");
        } else {
            for (Ruta ruta : rutas) {
                texto.append(ruta.getDestino())
                        .append(" | ").append(ruta.getMinutos()).append(" min")
                        .append(" | $").append(ruta.getCosto()).append('\n');
            }
        }

        resultadoConsulta.setText(texto.toString());
    }

    // Resuelve un viaje según el criterio elegido y reproduce el resultado calculado.
    @FXML
    private void consultarRecorrido() {
        animador.detener();
        mapa.mostrarRecorrido(null);

        String origen = origenRecorrido.getText()
                .trim().toUpperCase(Locale.ROOT);

        String destino = destinoRecorrido.getText()
                .trim().toUpperCase(Locale.ROOT);

        if (origen.isEmpty() || destino.isEmpty()) {
            resultadoConsulta.setText(
                    "Escribe los codigos de origen y destino.");
            return;
        }

        try {
            String criterio = criterioRecorrido.getValue();

            if (criterio == null) {
                resultadoConsulta.setText("Selecciona un criterio de busqueda.");
                return;
            }

            // Menos vuelos usa BFS; tiempo y costo comparten Dijkstra con distinto peso.
            ResultadoRecorrido resultado = switch (criterio) {
                case "Menos vuelos" ->
                    buscador.buscarMenosVuelos(origen, destino);

                case "Menor tiempo" ->
                    buscador.buscarMenorPeso(
                    origen, destino, CriterioPeso.MINUTOS);

                case "Menor costo" ->
                    buscador.buscarMenorPeso(
                    origen, destino, CriterioPeso.COSTO);

                default ->
                    throw new IllegalArgumentException(
                            "Criterio de busqueda desconocido");
            };

            // Los códigos existen, pero no hay un camino formado por conexiones activas.
            if (resultado == null) {
                resultadoConsulta.setText(
                        "No existe un recorrido de " + origen + " a " + destino + ".");
                return;
            }

            StringBuilder texto = new StringBuilder();

            texto.append("Criterio: ").append(criterio).append("\n\n");
            texto.append("Recorrido: ")
                    .append(String.join(" -> ", resultado.getCodigos()))
                    .append('\n');
            texto.append("Vuelos: ")
                    .append(resultado.getCantidadVuelos()).append('\n');
            texto.append("Tiempo total: ")
                    .append(resultado.getMinutosTotales()).append(" min\n");
            texto.append("Costo total: $")
                    .append(resultado.getCostoTotal()).append('\n');

            // Texto, resaltado y avión utilizan el mismo itinerario y sus tramos exactos.
            resultadoConsulta.setText(texto.toString());
            mapa.mostrarRecorrido(resultado);
            animador.reproducir(resultado);

        } catch (IllegalArgumentException e) {
            resultadoConsulta.setText(e.getMessage());
        }
    }

    // Consulta todos los destinos alcanzables desde el origen mediante DFS.
    @FXML
    private void consultarAlcanzables() {
        animador.detener();
        mapa.mostrarRecorrido(null);

        String origen = origenRecorrido.getText()
                .trim().toUpperCase(Locale.ROOT);

        if (origen.isEmpty()) {
            resultadoConsulta.setText(
                    "Escribe el codigo del aeropuerto de origen.");
            return;
        }

        try {
            Set<String> visitados = buscador.buscarAlcanzables(origen);

            List<String> alcanzables = new ArrayList<>();
            List<String> noAlcanzables = new ArrayList<>();

            for (Aeropuerto aeropuerto : grafo.obtenerAeropuertos()) {
                String codigo = aeropuerto.getCodigo();

                // El origen se muestra por separado de los destinos.
                if (codigo.equals(origen)) {
                    continue;
                }

                if (visitados.contains(codigo)) {
                    alcanzables.add(codigo);
                } else {
                    noAlcanzables.add(codigo);
                }
            }

            // El orden alfabético facilita la lectura; no representa el orden de visita de DFS.
            Collections.sort(alcanzables);
            Collections.sort(noAlcanzables);

            StringBuilder texto = new StringBuilder();

            texto.append("Origen: ").append(origen).append("\n\n");

            texto.append("Destinos alcanzables (")
                    .append(alcanzables.size()).append("):\n");

            if (alcanzables.isEmpty()) {
                texto.append("Ninguno");
            } else {
                texto.append(String.join(", ", alcanzables));
            }

            texto.append("\n\nDestinos no alcanzables (")
                    .append(noAlcanzables.size()).append("):\n");

            if (noAlcanzables.isEmpty()) {
                texto.append("Ninguno");
            } else {
                texto.append(String.join(", ", noAlcanzables));
            }

            resultadoConsulta.setText(texto.toString());
            mapa.mostrarAlcanzables(origen, visitados);

        } catch (IllegalArgumentException e) {
            resultadoConsulta.setText(e.getMessage());
        }
    }

    // Cancela una conexión directa; para un viaje con escalas se debe indicar uno de sus tramos.
    @FXML
    private void cancelarRuta() {
        animador.detener();
        String origen = origenRecorrido.getText()
                .trim().toUpperCase(Locale.ROOT);

        String destino = destinoRecorrido.getText()
                .trim().toUpperCase(Locale.ROOT);

        if (origen.isEmpty() || destino.isEmpty()) {
            resultadoConsulta.setText(
                    "Escribe los codigos de origen y destino.");
            return;
        }

        try {
            int cantidad = grafo.cancelarRuta(origen, destino);
            actualizarEstadoCarga();
            // Quita el viaje previo y redibuja las canceladas a partir del estado del grafo.
            mapa.mostrarRecorrido(null);

            if (cantidad == 0) {
                resultadoConsulta.setText(
                        "No existe una conexion directa activa de "
                        + origen + " a " + destino + "."
                        + "\nPara un recorrido con escalas, indica uno de sus tramos.");
            } else {
                resultadoConsulta.setText(
                        "Conexiones directas canceladas: " + cantidad
                        + "\n" + origen + " -> " + destino
                        + "\nLa cancelacion afecta solo esa direccion.");
            }

        } catch (IllegalArgumentException e) {
            resultadoConsulta.setText(e.getMessage());
        }
    }

    // Recupera todas las conexiones del respaldo en memoria, sin volver a leer los archivos.
    @FXML
    private void restaurarRutas() {
        animador.detener();
        int cantidad = grafo.restaurarRutas();
        actualizarEstadoCarga();
        mapa.mostrarRecorrido(null);

        if (cantidad == 0) {
            resultadoConsulta.setText(
                    "No hay rutas canceladas para restaurar.");
        } else {
            resultadoConsulta.setText(
                    "Conexiones restauradas: " + cantidad);
        }
    }

    // Los controles de reproducción delegan el estado de la secuencia al animador.
    @FXML
    private void pausarOContinuarVuelo() {
        animador.pausarOContinuar();
    }

    @FXML
    private void reiniciarVuelo() {
        animador.reiniciar();
    }
}
