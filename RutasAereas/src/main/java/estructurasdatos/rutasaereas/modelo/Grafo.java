package estructurasdatos.rutasaereas.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grafo {

    private final Map<String, Aeropuerto> aeropuertos = new HashMap<>();
    private final Map<String, List<Ruta>> adyacencias = new HashMap<>();
    private final Map<String, List<Ruta>> rutasOriginales = new HashMap<>();

    public void agregarAeropuerto(Aeropuerto aeropuerto) {
        String codigo = aeropuerto.getCodigo();

        if (aeropuertos.containsKey(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un aeropuerto con el codigo: " + codigo);
        }

        aeropuertos.put(codigo, aeropuerto);
        rutasOriginales.put(codigo, new ArrayList<>());

        // Cada vertice tiene su lista, aunque no tenga rutas salientes.
        adyacencias.put(codigo, new ArrayList<>());
    }

    public Aeropuerto buscarAeropuerto(String codigo) {
        return aeropuertos.get(codigo);
    }

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
        adyacencias.get(origen).add(ruta);
        rutasOriginales.get(origen).add(ruta);
    }

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

    public List<Ruta> obtenerRutasSalientes(String codigo) {
        List<Ruta> rutas = adyacencias.get(codigo);

        if (rutas == null) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto: " + codigo);
        }

        return List.copyOf(rutas);
    }

    public List<Aeropuerto> obtenerAeropuertos() {
        return List.copyOf(aeropuertos.values());
    }

    public int contarRutas() {
        int total = 0;

        for (List<Ruta> rutas : adyacencias.values()) {
            total += rutas.size();
        }

        return total;
    }
}
