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

public class AnimadorVuelo {

    private final Grafo grafo;
    private final MapaGrafo mapa;
    private final Label estadoVuelo;

    private SequentialTransition animacion;
    private ResultadoRecorrido recorrido;
    private String textoAntesDePausa;

    public AnimadorVuelo(
            Grafo grafo, MapaGrafo mapa, Label estadoVuelo) {
        this.grafo = grafo;
        this.mapa = mapa;
        this.estadoVuelo = estadoVuelo;
    }

    public void reproducir(ResultadoRecorrido recorrido) {
        detener();
        this.recorrido = recorrido;

        Aeropuerto origen = grafo.buscarAeropuerto(
                recorrido.getCodigos().get(0));

        mapa.mostrarAvion(origen.getX(), origen.getY(), 0);

        if (recorrido.getCantidadVuelos() == 0) {
            estadoVuelo.setText(
                    "Origen y destino coinciden.\n"
                    + "Vuelo finalizado: 0 min | $0.");
            return;
        }

        animacion = new SequentialTransition();

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

    private Timeline crearTramo(
            Aeropuerto origen, Aeropuerto destino, Ruta tramo,
            long minutosPrevios, long costoPrevio) {

        DoubleProperty progreso = new SimpleDoubleProperty(0);

        double angulo = Math.toDegrees(Math.atan2(
                (double) destino.getY() - origen.getY(),
                (double) destino.getX() - origen.getX()));

        Runnable actualizar = () -> {
            double fraccion = progreso.get();

            double x = origen.getX()
                    + ((double) destino.getX() - origen.getX()) * fraccion;

            double y = origen.getY()
                    + ((double) destino.getY() - origen.getY()) * fraccion;

            mapa.mostrarAvion(x, y, angulo);

            estadoVuelo.setText(String.format(
                    Locale.ROOT,
                    "Tramo: %s -> %s%n"
                    + "Acumulado: %.1f min | $%.2f",
                    origen.getCodigo(), destino.getCodigo(),
                    minutosPrevios + tramo.getMinutos() * fraccion,
                    costoPrevio + tramo.getCosto() * fraccion));
        };

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

    public void reiniciar() {
        if (recorrido != null) {
            reproducir(recorrido);
        }
    }

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