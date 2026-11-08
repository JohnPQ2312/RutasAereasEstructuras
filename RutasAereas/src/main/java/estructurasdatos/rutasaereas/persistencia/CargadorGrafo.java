package estructurasdatos.rutasaereas.persistencia;

import estructurasdatos.rutasaereas.modelo.Aeropuerto;
import estructurasdatos.rutasaereas.modelo.Grafo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Lee las cantidades y registros del formato de entrada, sin normalizar los códigos.
// Los datos incompletos o inválidos se comunican mediante IOException.
public class CargadorGrafo {

    // Construye los vértices antes de que las rutas puedan referirse a ellos.
    public Grafo cargarAeropuertos(Path archivo) throws IOException {
        Grafo grafo = new Grafo();

        // El recurso se cierra también si la lectura o una validación falla.
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

            // No se ignoran datos que excedan la cantidad declarada.
            if (lector.hasNext()) {
                throw new IOException(
                        "Hay datos adicionales a la cantidad declarada en "
                        + archivo);
            }

        } catch (NoSuchElementException | IllegalArgumentException exception) {
            // Conserva la causa de un campo inválido o de una validación del grafo.
            throw new IOException(
                    "Datos invalidos o incompletos en " + archivo,
                    exception);
        }

        return grafo;
    }

    // El orden importa: agregar una ruta exige que sus dos aeropuertos ya existan.
    public Grafo cargar(Path archivoAeropuertos, Path archivoRutas)
            throws IOException {

        Grafo grafo = cargarAeropuertos(archivoAeropuertos);
        cargarRutas(archivoRutas, grafo);

        return grafo;
    }

    // Grafo valida extremos y pesos al incorporar cada conexión dirigida.
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
