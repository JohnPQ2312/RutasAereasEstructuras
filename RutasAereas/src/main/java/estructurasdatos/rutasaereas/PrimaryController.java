package estructurasdatos.rutasaereas;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.modelo.Ruta;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PrimaryController {

    @FXML
    private Label estadoCarga;

    @FXML
    private TextField codigoAeropuerto;

    @FXML
    private TextArea resultadoConsulta;

    private Grafo grafo;

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;

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
}