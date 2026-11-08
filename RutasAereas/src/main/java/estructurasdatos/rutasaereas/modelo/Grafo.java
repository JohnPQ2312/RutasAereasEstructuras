package estructurasdatos.rutasaereas.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Grafo dirigido con dos pesos por conexión y listas de adyacencia por código.
// Mantiene las rutas activas y las originales sin modificar los archivos de entrada.
public class Grafo {

    // Localiza los datos del vértice sin depender de su posición en el mapa.
    private final Map<String, Aeropuerto> aeropuertos = new HashMap<>();
    // Las búsquedas recorren únicamente estas conexiones vigentes.
    private final Map<String, List<Ruta>> adyacencias = new HashMap<>();
    // Son listas distintas, pero comparten los mismos objetos Ruta con las activas.
    // Como las rutas no se modifican, basta conservar sus referencias para restaurarlas.
    private final Map<String, List<Ruta>> rutasOriginales = new HashMap<>();

    // Rechaza códigos repetidos y prepara listas incluso para un aeropuerto aislado.
    public void agregarAeropuerto(Aeropuerto aeropuerto) {
        String codigo = aeropuerto.getCodigo();

        if (aeropuertos.containsKey(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un aeropuerto con el codigo: " + codigo);
        }

        aeropuertos.put(codigo, aeropuerto);
        rutasOriginales.put(codigo, new ArrayList<>());

        adyacencias.put(codigo, new ArrayList<>());
    }

    // null indica que el código no está registrado.
    public Aeropuerto buscarAeropuerto(String codigo) {
        return aeropuertos.get(codigo);
    }

    // Ambos extremos deben existir y los pesos deben ser no negativos para Dijkstra.
    // Añade la conexión únicamente a la lista de salidas del origen.
    public void agregarRuta(String origen, String destino,
            int minutos, int costo) {

        if (!aeropuertos.containsKey(origen)) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto de origen: " + origen);
        }

        if (!aeropuertos.containsKey(destino)) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto de destino: " + destino);
        }

        if (minutos < 0 || costo < 0) {
            throw new IllegalArgumentException(
                    "Los minutos y el costo no pueden ser negativos");
        }

        Ruta ruta = new Ruta(destino, minutos, costo);
        // Las dos listas guardan la misma ruta, no dos copias de sus datos.
        adyacencias.get(origen).add(ruta);
        rutasOriginales.get(origen).add(ruta);
    }

    // Retira todas las coincidencias de esa dirección y devuelve cuántas eliminó.
    // Solo actúa sobre la lista de origen; el respaldo y los archivos se conservan.
    public int cancelarRuta(String origen, String destino) {
        if (!aeropuertos.containsKey(origen)
                || !aeropuertos.containsKey(destino)) {
            throw new IllegalArgumentException(
                    "El aeropuerto de origen o destino no existe");
        }

        List<Ruta> rutas = adyacencias.get(origen);
        int cantidadAnterior = rutas.size();

        rutas.removeIf(ruta -> ruta.getDestino().equals(destino));

        return cantidadAnterior - rutas.size();
    }

    // Repone las listas activas desde el respaldo y cuenta las conexiones recuperadas.
    public int restaurarRutas() {
        int cantidadRestaurada = 0;

        for (String codigo : aeropuertos.keySet()) {
            List<Ruta> activas = adyacencias.get(codigo);
            List<Ruta> originales = rutasOriginales.get(codigo);

            cantidadRestaurada += originales.size() - activas.size();

            activas.clear();
            activas.addAll(originales);
        }

        return cantidadRestaurada;
    }

    // Devuelve una lista que el llamador no puede modificar, sin clonar cada Ruta.
    // Copiar la colección evita que una consulta altere las adyacencias del grafo.
    public List<Ruta> obtenerRutasSalientes(String codigo) {
        List<Ruta> rutas = adyacencias.get(codigo);

        if (rutas == null) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto: " + codigo);
        }

        return List.copyOf(rutas);
    }

    // Consulta qué rutas del respaldo faltan en la lista activa para dibujarlas.
    public List<Ruta> obtenerRutasCanceladas(String codigo) {
        List<Ruta> originales = rutasOriginales.get(codigo);

        if (originales == null) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto: " + codigo);
        }

        // La comparación reconoce las referencias compartidas por ambas listas.
        Set<Ruta> activas = new HashSet<>(adyacencias.get(codigo));
        List<Ruta> canceladas = new ArrayList<>();

        for (Ruta ruta : originales) {
            if (!activas.contains(ruta)) {
                canceladas.add(ruta);
            }
        }

        return List.copyOf(canceladas);
    }

    // La copia permite recorrer los vértices sin exponer la colección del mapa.
    public List<Aeropuerto> obtenerAeropuertos() {
        return List.copyOf(aeropuertos.values());
    }

    // Suma los tamaños de las listas activas, sin recorrer cada objeto Ruta.
    public int contarRutas() {
        int total = 0;

        for (List<Ruta> rutas : adyacencias.values()) {
            total += rutas.size();
        }

        return total;
    }
}
