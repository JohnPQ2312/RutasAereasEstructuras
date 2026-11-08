package estructurasdatos.rutasaereas.vista;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.modelo.ResultadoRecorrido;
import estructurasdatos.rutasaereas.modelo.Ruta;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// Convierte los datos del grafo y del recorrido en capas de dibujo sobre un Canvas.
// El zoom, el arrastre y la animación no cambian las coordenadas guardadas en Aeropuerto.
public class MapaGrafo {

    private static final Color COLOR_RECORRIDO = Color.web("#007C91");
    private static final Color COLOR_CANCELADA = Color.web("#C62828");

    private final Canvas lienzo;
    private Grafo grafo;
    private ResultadoRecorrido recorridoSeleccionado;
    private Set<String> codigosResaltados = Set.of();
    // Estado de la vista: la ampliación es un factor; los desplazamientos están en píxeles.
    private double ampliacion = 1;
    private double desplazamientoX;
    private double desplazamientoY;
    private double ultimoX;
    private double ultimoY;
    private Set<String> alcanzablesMostrados; // null cuando no se está mostrando una consulta DFS.
    private String origenAlcanzabilidad;
    private boolean avionVisible;
    // Posición en coordenadas del archivo, antes de aplicar la transformación de la vista.
    private double avionX;
    private double avionY;
    private double anguloAvion; // Grados; cero apunta hacia la derecha.

    public MapaGrafo(Canvas lienzo) {
        this.lienzo = lienzo;

        // Un cambio de tamaño vuelve a calcular la escala sin mover los datos del modelo.
        lienzo.widthProperty().addListener(
                (observable, anterior, nuevo) -> dibujar());

        lienzo.heightProperty().addListener(
                (observable, anterior, nuevo) -> dibujar());

        lienzo.setCursor(Cursor.OPEN_HAND);
        lienzo.setOnScroll(this::ampliarMapa);

        lienzo.setOnMousePressed(evento -> {
            if (evento.isPrimaryButtonDown()) {
                ultimoX = evento.getX();
                ultimoY = evento.getY();
            }
        });

        lienzo.setOnMouseDragged(evento -> {
            if (evento.isPrimaryButtonDown()) {
                // El arrastre desplaza la vista según lo recorrido desde el último evento.
                desplazamientoX += evento.getX() - ultimoX;
                desplazamientoY += evento.getY() - ultimoY;
                ultimoX = evento.getX();
                ultimoY = evento.getY();
                lienzo.setCursor(Cursor.CLOSED_HAND);
                dibujar();
            }
        });

        lienzo.setOnMouseReleased(evento
                -> lienzo.setCursor(Cursor.OPEN_HAND));
    }

    // Al cambiar de red se descartan los resaltados y se recupera la vista inicial.
    public void setGrafo(Grafo grafo) {
        avionVisible = false;
        this.grafo = grafo;
        recorridoSeleccionado = null;
        codigosResaltados = Set.of();
        alcanzablesMostrados = null;
        origenAlcanzabilidad = null;
        restablecerVista();
    }

    // null limpia el recorrido. El controlador detiene el animador por separado.
    // Una consulta de viaje reemplaza los colores de alcanzabilidad anteriores.
    public void mostrarRecorrido(ResultadoRecorrido recorrido) {
        avionVisible = false;
        alcanzablesMostrados = null;
        origenAlcanzabilidad = null;
        recorridoSeleccionado = recorrido;

        if (recorrido == null) {
            codigosResaltados = Set.of();
        } else {
            codigosResaltados = Set.copyOf(recorrido.getCodigos());
        }

        dibujar();
    }

    // Alterna a los colores de DFS y conserva una copia del conjunto recibido.
    public void mostrarAlcanzables(String origen, Set<String> alcanzables) {
        avionVisible = false;
        origenAlcanzabilidad = origen;
        alcanzablesMostrados = Set.copyOf(alcanzables);

        recorridoSeleccionado = null;
        codigosResaltados = Set.of();

        dibujar();
    }

    // Recibe coordenadas del mapa; dibujarAvion las convierte a píxeles del Canvas.
    public void mostrarAvion(double x, double y, double angulo) {
        avionX = x;
        avionY = y;
        anguloAvion = angulo;
        avionVisible = true;
        dibujar();
    }

    public void ocultarAvion() {
        avionVisible = false;
        dibujar();
    }

    // El origen de DFS tiene prioridad; después se aplican alcanzabilidad o recorrido.
    private Color obtenerColorAeropuerto(String codigo) {
        if (codigo.equals(origenAlcanzabilidad)) {
            return Color.web("#7B1FA2");
        }

        if (alcanzablesMostrados != null) {
            if (alcanzablesMostrados.contains(codigo)) {
                return Color.web("#2E7D32");
            }

            return Color.web("#C62828");
        }

        if (codigosResaltados.contains(codigo)) {
            return COLOR_RECORRIDO;
        }

        return Color.web("#1565C0");
    }

    // Recupera la escala inicial y el centrado sin cambiar el recorrido ni la red.
    public void restablecerVista() {
        ampliacion = 1;
        desplazamientoX = 0;
        desplazamientoY = 0;
        dibujar();
    }

    private void ampliarMapa(ScrollEvent evento) {
        if (evento.getDeltaY() == 0) {
            return;
        }

        double anterior = ampliacion;
        double factor = evento.getDeltaY() > 0 ? 1.25 : 0.8;
        ampliacion = Math.max(1, Math.min(8, ampliacion * factor));

        if (ampliacion == 1) {
            desplazamientoX = 0;
            desplazamientoY = 0;
        } else {
            // Conserva debajo del cursor el lugar que se está ampliando.
            double relacion = ampliacion / anterior;
            desplazamientoX = relacion * desplazamientoX
                    + (1 - relacion) * (evento.getX() - lienzo.getWidth() / 2);
            desplazamientoY = relacion * desplazamientoY
                    + (1 - relacion) * (evento.getY() - lienzo.getHeight() / 2);
        }

        dibujar();
        evento.consume();
    }

    private boolean esVisible(double x, double y) {
        return x >= 0 && x <= lienzo.getWidth()
                && y >= 0 && y <= lienzo.getHeight();
    }

    // Redibuja el estado completo: conexiones, resaltados, puntos, etiquetas y avión.
    public void dibujar() {
        GraphicsContext dibujo = lienzo.getGraphicsContext2D();

        dibujo.setFill(Color.web("#EEF4F8"));
        dibujo.fillRect(0, 0, lienzo.getWidth(), lienzo.getHeight());

        if (grafo == null
                || lienzo.getWidth() <= 40
                || lienzo.getHeight() <= 40) {
            return;
        }

        // La misma escala en X e Y conserva las proporciones de la referencia 1000 por 1000.
        double escala = Math.min(
                (lienzo.getWidth() - 40) / 1000.0,
                (lienzo.getHeight() - 40) / 1000.0) * ampliacion;

        // Centra la referencia en el lienzo y después incorpora el desplazamiento del arrastre.
        double margenX = (lienzo.getWidth() - 1000 * escala) / 2
                + desplazamientoX;
        double margenY = (lienzo.getHeight() - 1000 * escala) / 2
                + desplazamientoY;

        dibujo.setFont(Font.font(12));
        dibujo.setStroke(Color.rgb(100, 130, 150, 0.22));
        dibujo.setLineWidth(0.8);

        // Las conexiones activas forman el fondo; los resaltados se superponen después.
        for (Aeropuerto origen : grafo.obtenerAeropuertos()) {
            double xOrigen = margenX + origen.getX() * escala;
            double yOrigen = margenY + origen.getY() * escala;

            for (Ruta ruta : grafo.obtenerRutasSalientes(origen.getCodigo())) {
                Aeropuerto destino = grafo.buscarAeropuerto(ruta.getDestino());

                double xDestino = margenX + destino.getX() * escala;
                double yDestino = margenY + destino.getY() * escala;

                dibujo.strokeLine(xOrigen, yOrigen, xDestino, yDestino);
            }
        }

        dibujarRecorrido(dibujo, escala, margenX, margenY);
        // El punteado permanece visible aunque se resalte la ruta inversa.
        dibujarCanceladas(dibujo, escala, margenX, margenY);

        List<Rectangle2D> espaciosOcupados = new ArrayList<>();

        for (Aeropuerto aeropuerto : grafo.obtenerAeropuertos()) {
            double x = margenX + aeropuerto.getX() * escala;
            double y = margenY + aeropuerto.getY() * escala;

            if (!esVisible(x, y)) {
                continue;
            }

            dibujo.setFill(obtenerColorAeropuerto(aeropuerto.getCodigo()));
            dibujo.fillOval(x - 4, y - 4, 8, 8);

            // Reserva todos los puntos antes de ubicar las etiquetas para que puedan evitarlos.
            espaciosOcupados.add(new Rectangle2D(x - 6, y - 6, 12, 12));
        }

        for (Aeropuerto aeropuerto : grafo.obtenerAeropuertos()) {
            double x = margenX + aeropuerto.getX() * escala;
            double y = margenY + aeropuerto.getY() * escala;

            if (!esVisible(x, y)) {
                continue;
            }

            dibujarEtiqueta(
                    dibujo, aeropuerto.getCodigo(), x, y, espaciosOcupados);
        }

        // El avión queda por encima de las demás capas.
        dibujarAvion(dibujo, escala, margenX, margenY);
    }

    private void dibujarAvion(
            GraphicsContext dibujo,
            double escala, double margenX, double margenY) {

        if (!avionVisible) {
            return;
        }

        double x = margenX + avionX * escala;
        double y = margenY + avionY * escala;

        // La transformación es local al avión; restore recupera el sistema de coordenadas anterior.
        dibujo.save();
        dibujo.translate(x, y);
        dibujo.rotate(anguloAvion);

        // Figura de avión de papel orientada inicialmente hacia la derecha.
        double[] puntosX = {12, -8, -3, -8};
        double[] puntosY = {0, -8, 0, 8};

        dibujo.setFill(Color.web("#0D47A1"));
        dibujo.fillPolygon(puntosX, puntosY, 4);

        dibujo.setStroke(Color.WHITE);
        dibujo.setLineWidth(1.5);
        dibujo.setLineDashes();
        dibujo.strokePolygon(puntosX, puntosY, 4);

        dibujo.restore();
    }

    private void dibujarRecorrido(
            GraphicsContext dibujo,
            double escala, double margenX, double margenY) {

        if (recorridoSeleccionado == null) {
            return;
        }

        dibujo.setStroke(COLOR_RECORRIDO);
        dibujo.setLineWidth(4);

        List<String> codigos = recorridoSeleccionado.getCodigos();

        for (int i = 0; i < codigos.size() - 1; i++) {
            Aeropuerto origen = grafo.buscarAeropuerto(codigos.get(i));
            Aeropuerto destino = grafo.buscarAeropuerto(codigos.get(i + 1));

            dibujarTramo(dibujo, origen, destino, escala, margenX, margenY);
        }
    }

    private void dibujarCanceladas(
            GraphicsContext dibujo,
            double escala, double margenX, double margenY) {

        // El punteado y su grosor no deben propagarse al resto del dibujo.
        dibujo.save();
        dibujo.setStroke(COLOR_CANCELADA);
        dibujo.setLineWidth(2.5);
        dibujo.setLineDashes(6, 4);

        for (Aeropuerto origen : grafo.obtenerAeropuertos()) {
            for (Ruta ruta : grafo.obtenerRutasCanceladas(origen.getCodigo())) {
                Aeropuerto destino = grafo.buscarAeropuerto(ruta.getDestino());
                dibujarTramo(dibujo, origen, destino, escala, margenX, margenY);
            }
        }

        dibujo.restore();
    }

    // Recorrido y cancelación comparten extremos y posición de flecha.
    private void dibujarTramo(
            GraphicsContext dibujo, Aeropuerto origen, Aeropuerto destino,
            double escala, double margenX, double margenY) {

        double xOrigen = margenX + origen.getX() * escala;
        double yOrigen = margenY + origen.getY() * escala;
        double xDestino = margenX + destino.getX() * escala;
        double yDestino = margenY + destino.getY() * escala;

        // Los extremos coincidentes se representan con un círculo.
        if (xOrigen == xDestino && yOrigen == yDestino) {
            dibujo.strokeOval(xOrigen - 10, yOrigen - 20, 20, 20);
            return;
        }

        dibujo.save();
        dibujo.strokeLine(xOrigen, yOrigen, xDestino, yDestino);
        // La flecha permanece continua aunque el tramo sea discontinuo.
        dibujo.setLineDashes();
        dibujarFlecha(dibujo, xOrigen, yOrigen, xDestino, yDestino);
        dibujo.restore();
    }

    private void dibujarFlecha(
            GraphicsContext dibujo,
            double xOrigen, double yOrigen,
            double xDestino, double yDestino) {

        double angulo = Math.atan2(
                yDestino - yOrigen, xDestino - xOrigen);
        // La punta indica el sentido antes de llegar al extremo del destino.
        double xFlecha = xOrigen + (xDestino - xOrigen) * 0.7;
        double yFlecha = yOrigen + (yDestino - yOrigen) * 0.7;

        double longitud = 10;
        double apertura = Math.PI / 6;

        dibujo.strokeLine(
                xFlecha, yFlecha,
                xFlecha - longitud * Math.cos(angulo - apertura),
                yFlecha - longitud * Math.sin(angulo - apertura));

        dibujo.strokeLine(
                xFlecha, yFlecha,
                xFlecha - longitud * Math.cos(angulo + apertura),
                yFlecha - longitud * Math.sin(angulo + apertura));
    }

    // Busca espacio para el código respetando los puntos y las etiquetas ya colocadas.
    private void dibujarEtiqueta(
            GraphicsContext dibujo, String codigo,
            double x, double y, List<Rectangle2D> espaciosOcupados) {

        Text medida = new Text(codigo);
        medida.setFont(dibujo.getFont());

        double ancho = medida.getLayoutBounds().getWidth() + 8;
        double alto = 20;
        Rectangle2D area = null;

        // Prueba posiciones alrededor del punto, aumentando la distancia.
        for (int distancia = 8;
                distancia <= 128 && area == null;
                distancia += 12) {

            double[][] posiciones = {
                {x + distancia, y - alto - distancia},
                {x + distancia, y - alto / 2},
                {x + distancia, y + distancia},
                {x - ancho / 2, y + distancia},
                {x - ancho - distancia, y + distancia},
                {x - ancho - distancia, y - alto / 2},
                {x - ancho - distancia, y - alto - distancia},
                {x - ancho / 2, y - alto - distancia}
            };

            for (double[] posicion : posiciones) {
                Rectangle2D candidata = new Rectangle2D(
                        posicion[0], posicion[1], ancho, alto);

                if (candidata.getMinX() < 2
                        || candidata.getMinY() < 2
                        || candidata.getMaxX() > lienzo.getWidth() - 2
                        || candidata.getMaxY() > lienzo.getHeight() - 2) {
                    continue;
                }

                boolean libre = espaciosOcupados.stream().noneMatch(
                        ocupado -> ocupado.intersects(candidata));

                if (libre) {
                    area = candidata;
                    break;
                }
            }
        }

        // Si ninguna candidata queda libre, conserva una posición dentro del lienzo.
        if (area == null) {
            double izquierda = Math.max(
                    2, Math.min(x + 8, lienzo.getWidth() - ancho - 2));

            double arriba = Math.max(
                    2, Math.min(y - alto, lienzo.getHeight() - alto - 2));

            area = new Rectangle2D(izquierda, arriba, ancho, alto);
        }

        dibujo.setStroke(Color.web("#78909C"));
        dibujo.setLineWidth(0.6);
        dibujo.strokeLine(
                x, y,
                area.getMinX() + ancho / 2,
                area.getMinY() + alto / 2);

        dibujo.setFill(Color.WHITE);
        dibujo.fillRoundRect(
                area.getMinX(), area.getMinY(), ancho, alto, 6, 6);

        dibujo.setFill(Color.web("#172B4D"));
        dibujo.fillText(codigo, area.getMinX() + 4, area.getMinY() + 14);

        espaciosOcupados.add(area);
    }
}
