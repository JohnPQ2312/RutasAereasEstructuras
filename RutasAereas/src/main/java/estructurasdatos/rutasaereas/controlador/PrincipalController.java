package estructurasdatos.rutasaereas.controlador;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.modelo.Ruta;
import estructurasdatos.rutasaereas.algoritmos.BuscadorRutas;
import estructurasdatos.rutasaereas.modelo.ResultadoRecorrido;
import estructurasdatos.rutasaereas.algoritmos.CriterioPeso;
import javafx.scene.control.ComboBox;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PrincipalController {

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

    private BuscadorRutas buscador;

    private Grafo grafo;

    @FXML
    private void initialize() {
        criterioRecorrido.getItems().setAll(
                "Menos vuelos",
                "Menor tiempo",
                "Menor costo");

        criterioRecorrido.getSelectionModel().selectFirst();
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        this.buscador = new BuscadorRutas(grafo);

        estadoCarga.setText(
                "Aeropuertos: " + grafo.obtenerAeropuertos().size()
                + " | Rutas: " + grafo.contarRutas());
    }

    @FXML
    private void consultarAeropuerto() {
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

    @FXML
    private void consultarRecorrido() {
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

            resultadoConsulta.setText(texto.toString());

        } catch (IllegalArgumentException e) {
            resultadoConsulta.setText(e.getMessage());
        }
    }
}
