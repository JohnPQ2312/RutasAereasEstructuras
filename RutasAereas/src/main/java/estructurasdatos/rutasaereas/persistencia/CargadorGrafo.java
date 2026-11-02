package estructurasdatos.rutasaereas.persistencia;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class CargadorGrafo {

    public Grafo cargarAeropuertos(Path archivo) throws IOException {
        Grafo grafo = new Grafo();

        try (Scanner lector = new Scanner(archivo, StandardCharsets.UTF_8)) {
            int cantidad = lector.nextInt();

            if (cantidad < 0) {
                throw new IOException(
                        "La cantidad de aeropuertos no puede ser negativa");
            }

            for (int i = 0; i < cantidad; i++) {
                String codigo = lector.next();
                int x = lector.nextInt();
                int y = lector.nextInt();
                String ciudad = lector.next();
                String pais = lector.next();

                Aeropuerto aeropuerto = new Aeropuerto(
                        codigo, x, y, ciudad, pais);

                grafo.agregarAeropuerto(aeropuerto);
            }

            if (lector.hasNext()) {
                throw new IOException(
                        "Hay datos adicionales a la cantidad declarada en "
                        + archivo);
            }

        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new IOException(
                    "Datos invalidos o incompletos en " + archivo,
                    exception);
        }

        return grafo;
    }

    public Grafo cargar(Path archivoAeropuertos, Path archivoRutas)
            throws IOException {

        Grafo grafo = cargarAeropuertos(archivoAeropuertos);
        cargarRutas(archivoRutas, grafo);

        return grafo;
    }

    private void cargarRutas(Path archivo, Grafo grafo) throws IOException {

        try (Scanner lector = new Scanner(archivo, StandardCharsets.UTF_8)) {
            int cantidad = lector.nextInt();

            if (cantidad < 0) {
                throw new IOException(
                        "La cantidad de rutas no puede ser negativa");
            }

            for (int i = 0; i < cantidad; i++) {
                String origen = lector.next();
                String destino = lector.next();
                int minutos = lector.nextInt();
                int costo = lector.nextInt();

                grafo.agregarRuta(origen, destino, minutos, costo);
            }

            if (lector.hasNext()) {
                throw new IOException(
                        "Hay datos adicionales a la cantidad declarada en "
                        + archivo);
            }

        } catch (NoSuchElementException | IllegalArgumentException exception) {
            throw new IOException(
                    "Datos invalidos o incompletos en " + archivo,
                    exception);
        }
    }
}
