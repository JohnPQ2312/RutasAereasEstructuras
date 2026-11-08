package estructurasdatos.rutasaereas.vista;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.modelo.ResultadoRecorrido;
import estructurasdatos.rutasaereas.modelo.Ruta;
import java.util.Locale;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

// Reproduce un itinerario ya calculado; no ejecuta búsquedas ni modifica el grafo.
// JavaFX actualiza el progreso de cada tramo y realiza las pausas en las escalas.
public class AnimadorVuelo {

    private final Grafo grafo;
    private final MapaGrafo mapa;
    private final Label estadoVuelo;

    private SequentialTransition animacion;
    // Conserva el último recorrido para poder reiniciarlo incluso después de terminar.
    private ResultadoRecorrido recorrido;
    private String textoAntesDePausa;

    public AnimadorVuelo(
            Grafo grafo, MapaGrafo mapa, Label estadoVuelo) {
        this.grafo = grafo;
        this.mapa = mapa;
        this.estadoVuelo = estadoVuelo;
    }

    // Detiene la reproducción anterior y construye una secuencia nueva desde el origen.
    public void reproducir(ResultadoRecorrido recorrido) {
        detener();
        this.recorrido = recorrido;

        Aeropuerto origen = grafo.buscarAeropuerto(
                recorrido.getCodigos().get(0));

        mapa.mostrarAvion(origen.getX(), origen.getY(), 0);

        // Origen y destino iguales no necesitan transiciones ni escalas.
        if (recorrido.getCantidadVuelos() == 0) {
            estadoVuelo.setText(
                    "Origen y destino coinciden.\n"
                    + "Vuelo finalizado: 0 min | $0.");
            return;
        }

        animacion = new SequentialTransition();

        // Cada tramo recibe lo acumulado antes de salir de su aeropuerto de origen.
        long minutosPrevios = 0;
        long costoPrevio = 0;

        for (int i = 0; i < recorrido.getCantidadVuelos(); i++) {
            Ruta tramo = recorrido.getTramos().get(i);

            Aeropuerto salida = grafo.buscarAeropuerto(
                    recorrido.getCodigos().get(i));

            Aeropuerto llegada = grafo.buscarAeropuerto(
                    tramo.getDestino());

            Timeline vuelo = crearTramo(
                    salida, llegada, tramo, minutosPrevios, costoPrevio);

            animacion.getChildren().add(vuelo);

            // La escala de un segundo se añade entre vuelos, no al llegar al destino final.
            if (i < recorrido.getCantidadVuelos() - 1) {
                animacion.getChildren().add(
                        new PauseTransition(Duration.seconds(1)));
            }

            minutosPrevios += tramo.getMinutos();
            costoPrevio += tramo.getCosto();
        }

        animacion.setOnFinished(evento ->
                estadoVuelo.setText(
                        "Vuelo finalizado.\n"
                        + "Minutos: " + recorrido.getMinutosTotales()
                        + " | Costo: $" + recorrido.getCostoTotal()));

        animacion.play();
    }

    // Tres segundos de animación representan un vuelo completo, sin importar sus minutos registrados.
    private Timeline crearTramo(
            Aeropuerto origen, Aeropuerto destino, Ruta tramo,
            long minutosPrevios, long costoPrevio) {

        DoubleProperty progreso = new SimpleDoubleProperty(0); // 0: origen; 1: destino.

        // El ángulo usa las mismas coordenadas del mapa, con Y hacia abajo.
        double angulo = Math.toDegrees(Math.atan2(
                (double) destino.getY() - origen.getY(),
                (double) destino.getX() - origen.getX()));

        Runnable actualizar = () -> {
            double fraccion = progreso.get();

            // La misma fracción determina la posición y la parte consumida de ambos pesos.
            double x = origen.getX()
                    + ((double) destino.getX() - origen.getX()) * fraccion;

            double y = origen.getY()
                    + ((double) destino.getY() - origen.getY()) * fraccion;

            mapa.mostrarAvion(x, y, angulo);

            // Los vuelos anteriores se suman completos; solo el tramo actual es proporcional.
            estadoVuelo.setText(String.format(
                    Locale.ROOT,
                    "Tramo: %s -> %s%n"
                    + "Acumulado: %.1f min | $%.2f",
                    origen.getCodigo(), destino.getCodigo(),
                    minutosPrevios + tramo.getMinutos() * fraccion,
                    costoPrevio + tramo.getCosto() * fraccion));
        };

        // Timeline cambia la propiedad; este listener actualiza el mapa y el panel.
        progreso.addListener(
                (observable, anterior, nuevo) -> actualizar.run());

        Timeline vuelo = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        evento -> actualizar.run(),
                        new KeyValue(progreso, 0, Interpolator.LINEAR)),
                new KeyFrame(
                        Duration.seconds(3),
                        new KeyValue(progreso, 1, Interpolator.LINEAR)));

        vuelo.setOnFinished(evento ->
                estadoVuelo.setText(
                        "Llegada a " + destino.getCodigo() + "\n"
                        + "Acumulado: "
                        + (minutosPrevios + tramo.getMinutos())
                        + " min | $"
                        + (costoPrevio + tramo.getCosto())));

        return vuelo;
    }

    // Pausar la secuencia también detiene una escala que esté en curso.
    public void pausarOContinuar() {
        if (animacion == null) {
            return;
        }

        if (animacion.getStatus() == Animation.Status.RUNNING) {
            animacion.pause();
            textoAntesDePausa = estadoVuelo.getText();
            estadoVuelo.setText("En pausa.\n" + textoAntesDePausa);

        } else if (animacion.getStatus() == Animation.Status.PAUSED) {
            estadoVuelo.setText(textoAntesDePausa);
            animacion.play();
        }
    }

    // Recrea todos los tramos; no continúa desde la posición que tenía el avión.
    public void reiniciar() {
        if (recorrido != null) {
            reproducir(recorrido);
        }
    }

    // El controlador lo llama al consultar o cambiar el grafo.
    // Primero detiene la secuencia para que no vuelva a dibujar el avión después de ocultarlo.
    public void detener() {
        if (animacion != null) {
            animacion.stop();
        }

        animacion = null;
        recorrido = null;
        textoAntesDePausa = null;

        mapa.ocultarAvion();
        estadoVuelo.setText("Sin vuelo activo.");
    }
}
