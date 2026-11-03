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
import java.util.PriorityQueue;

public class BuscadorRutas {

    private final Grafo grafo;

    private static class EntradaPrioridad implements Comparable<EntradaPrioridad> {

        private final String codigo;
        private final long valorAcumulado;

        private EntradaPrioridad(String codigo, long valorAcumulado) {
            this.codigo = codigo;
            this.valorAcumulado = valorAcumulado;
        }

        @Override
        public int compareTo(EntradaPrioridad otra) {
            return Long.compare(this.valorAcumulado, otra.valorAcumulado);
        }
    }

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

    public ResultadoRecorrido buscarMenorPeso(
            String origen, String destino, CriterioPeso criterio) {

        if (grafo.buscarAeropuerto(origen) == null
                || grafo.buscarAeropuerto(destino) == null) {
            throw new IllegalArgumentException(
                    "El aeropuerto de origen o destino no existe");
        }

        if (criterio == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar un criterio de peso");
        }

        PriorityQueue<EntradaPrioridad> pendientes = new PriorityQueue<>();
        Map<String, Long> mejoresValores = new HashMap<>();
        Map<String, String> predecesores = new HashMap<>();
        Map<String, Ruta> rutasLlegada = new HashMap<>();

        mejoresValores.put(origen, 0L);
        pendientes.add(new EntradaPrioridad(origen, 0L));

        while (!pendientes.isEmpty()) {
            EntradaPrioridad entrada = pendientes.remove();
            String actual = entrada.codigo;

            // Una mejora posterior puede haber dejado esta entrada desactualizada.
            if (entrada.valorAcumulado != mejoresValores.get(actual)) {
                continue;
            }

            if (actual.equals(destino)) {
                return reconstruirRecorrido(
                        origen, destino, predecesores, rutasLlegada);
            }

            for (Ruta ruta : grafo.obtenerRutasSalientes(actual)) {
                String vecino = ruta.getDestino();

                long nuevoValor = entrada.valorAcumulado
                        + criterio.obtenerPeso(ruta);

                long mejorConocido = mejoresValores.getOrDefault(
                        vecino, Long.MAX_VALUE);

                if (nuevoValor < mejorConocido) {
                    mejoresValores.put(vecino, nuevoValor);
                    predecesores.put(vecino, actual);
                    rutasLlegada.put(vecino, ruta);

                    pendientes.add(
                            new EntradaPrioridad(vecino, nuevoValor));
                }
            }
        }

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
