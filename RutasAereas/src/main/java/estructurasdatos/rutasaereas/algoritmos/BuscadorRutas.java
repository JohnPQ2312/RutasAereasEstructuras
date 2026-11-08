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
import java.util.Deque;

// Resuelve consultas sobre las conexiones activas; no dibuja ni modifica el grafo.
public class BuscadorRutas {

    private final Grafo grafo;

    // Conserva el costo que tenía una propuesta al entrar en la cola.
    // Una mejora posterior se guarda en otra entrada, sin cambiar esta.
    private static class EntradaPrioridad implements Comparable<EntradaPrioridad> {

        private final String codigo;
        private final long valorAcumulado;

        private EntradaPrioridad(String codigo, long valorAcumulado) {
            this.codigo = codigo;
            this.valorAcumulado = valorAcumulado;
        }

        @Override
        public int compareTo(EntradaPrioridad otra) {
            // La prioridad depende del peso acumulado, no del orden de llegada.
            return Long.compare(this.valorAcumulado, otra.valorAcumulado);
        }
    }

    public BuscadorRutas(Grafo grafo) {
        this.grafo = grafo;
    }

    // BFS explora por cantidad de vuelos, sin comparar minutos ni dólares.
    // Devuelve null si los dos códigos existen pero no hay camino entre ellos.
    public ResultadoRecorrido buscarMenosVuelos(String origen, String destino) {
        if (grafo.buscarAeropuerto(origen) == null
                || grafo.buscarAeropuerto(destino) == null) {
            throw new IllegalArgumentException(
                    "El aeropuerto de origen o destino no existe");
        }

        Queue<String> pendientes = new ArrayDeque<>();
        Set<String> descubiertos = new HashSet<>();
        // El predecesor permite retroceder; la ruta exacta conserva sus dos pesos.
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

        // La cola se agotó sin alcanzar el destino.
        return null;
    }

    // Dijkstra minimiza el peso elegido; Grafo rechaza conexiones de peso negativo.
    // Como BFS, devuelve null cuando el destino no es alcanzable.
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
        // Este mapa guarda el costo vigente; la cola puede conservar propuestas antiguas.
        Map<String, Long> mejoresValores = new HashMap<>();
        Map<String, String> predecesores = new HashMap<>();
        Map<String, Ruta> rutasLlegada = new HashMap<>();

        mejoresValores.put(origen, 0L);
        pendientes.add(new EntradaPrioridad(origen, 0L));

        while (!pendientes.isEmpty()) {
            EntradaPrioridad entrada = pendientes.remove();
            String actual = entrada.codigo;

            // Cada mejora añade otra entrada. Se descarta la antigua si ya no es la mejor.
            if (entrada.valorAcumulado != mejoresValores.get(actual)) {
                continue;
            }

            // Al extraer la menor propuesta vigente, su costo es definitivo con pesos no negativos.
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

                // Un empate conserva el camino conocido; solo una mejora lo reemplaza.
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

    // DFS utiliza una pila y devuelve un conjunto sin orden de visita que incluye al origen.
    public Set<String> buscarAlcanzables(String origen) {
        if (grafo.buscarAeropuerto(origen) == null) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto: " + origen);
        }

        Deque<String> pendientes = new ArrayDeque<>();
        Set<String> visitados = new HashSet<>();

        pendientes.push(origen);

        while (!pendientes.isEmpty()) {
            String actual = pendientes.pop();

            // Se marca al extraer. Si el aeropuerto se repite en la pila, no vuelve a expandirse.
            if (!visitados.add(actual)) {
                continue;
            }

            for (Ruta ruta : grafo.obtenerRutasSalientes(actual)) {
                String vecino = ruta.getDestino();

                if (!visitados.contains(vecino)) {
                    pendientes.push(vecino);
                }
            }
        }

        return Set.copyOf(visitados);
    }

    // Recupera las conexiones elegidas, sin buscar de nuevo una ruta entre cada par de códigos.
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

        // Las conexiones se recuperaron desde la última hasta la primera.
        Collections.reverse(tramos);

        return new ResultadoRecorrido(origen, tramos);
    }
}
