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
}