package estructurasdatos.rutasaereas.algoritmos;

import estructurasdatos.rutasaereas.modelo.Grafo;
import estructurasdatos.rutasaereas.modelo.ResultadoRecorrido;
import estructurasdatos.rutasaereas.modelo.Ruta;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BuscadorRutas {

    private final Grafo grafo;

    public BuscadorRutas(Grafo grafo) {
        this.grafo = grafo;
    }

    public ResultadoRecorrido buscarMenosVuelos(String origen, String destino) {
        if (grafo.buscarAeropuerto(origen) == null
                || grafo.buscarAeropuerto(destino) == null) {
            throw new IllegalArgumentException(
                    "El aeropuerto de origen o destino no existe");
        }

        Queue<String> pendientes = new ArrayDeque<>();
        Set<String> descubiertos = new HashSet<>();
        Map<String, String> predecesores = new HashMap<>();
        Map<String, Ruta> rutasLlegada = new HashMap<>();

        descubiertos.add(origen);
        pendientes.add(origen);

        while (!pendientes.isEmpty()) {
            String actual = pendientes.remove();

            if (actual.equals(destino)) {
                return reconstruirRecorrido(
                        origen, destino, predecesores, rutasLlegada);
            }

            for (Ruta ruta : grafo.obtenerRutasSalientes(actual)) {
                String vecino = ruta.getDestino();

                // Se marca al encolar para evitar entradas repetidas.
                if (descubiertos.add(vecino)) {
                    predecesores.put(vecino, actual);
                    rutasLlegada.put(vecino, ruta);
                    pendientes.add(vecino);
                }
            }
        }

        // La cola se agoto sin alcanzar el destino.
        return null;
    }

    private ResultadoRecorrido reconstruirRecorrido(
            String origen, String destino,
            Map<String, String> predecesores,
            Map<String, Ruta> rutasLlegada) {

        List<Ruta> tramos = new ArrayList<>();
        String actual = destino;

        // Los predecesores permiten volver del destino hasta el origen.
        while (!actual.equals(origen)) {
            tramos.add(rutasLlegada.get(actual));
            actual = predecesores.get(actual);
        }

        // Las conexiones se recuperaron desde la ultima hasta la primera.
        Collections.reverse(tramos);

        return new ResultadoRecorrido(origen, tramos);
    }
}