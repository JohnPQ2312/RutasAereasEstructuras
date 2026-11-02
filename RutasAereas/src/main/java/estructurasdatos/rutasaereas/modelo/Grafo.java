package estructurasdatos.rutasaereas.modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grafo {

    private final Map<String, Aeropuerto> aeropuertos = new HashMap<>();
    private final Map<String, List<Ruta>> adyacencias = new HashMap<>();

    public void agregarAeropuerto(Aeropuerto aeropuerto) {
        String codigo = aeropuerto.getCodigo();

        if (aeropuertos.containsKey(codigo)) {
            throw new IllegalArgumentException(
                    "Ya existe un aeropuerto con el codigo: " + codigo);
        }

        aeropuertos.put(codigo, aeropuerto);

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
    }

    public List<Ruta> obtenerRutasSalientes(String codigo) {
        List<Ruta> rutas = adyacencias.get(codigo);

        if (rutas == null) {
            throw new IllegalArgumentException(
                    "No existe el aeropuerto: " + codigo);
        }

        return List.copyOf(rutas);
    }
}
