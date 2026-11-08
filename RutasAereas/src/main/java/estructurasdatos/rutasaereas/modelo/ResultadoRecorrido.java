package estructurasdatos.rutasaereas.modelo;

import java.util.ArrayList;
import java.util.List;

// Itinerario ya resuelto: la interfaz y el animador utilizan los mismos tramos.
public class ResultadoRecorrido {

    private final List<String> codigos;
    private final List<Ruta> tramos;
    // Se acumulan en long los pesos int de los vuelos del itinerario.
    private final long minutosTotales;
    private final long costoTotal;

    public ResultadoRecorrido(String origen, List<Ruta> tramos) {
        // Conserva referencias a Ruta; la lista no permite añadir, quitar ni reemplazar tramos.
        this.tramos = List.copyOf(tramos);

        List<String> recorrido = new ArrayList<>();
        recorrido.add(origen);

        long minutos = 0;
        long costo = 0;

        // Cada conexión agrega un destino y sus dos pesos al resultado.
        for (Ruta tramo : this.tramos) {
            recorrido.add(tramo.getDestino());
            minutos += tramo.getMinutos();
            costo += tramo.getCosto();
        }

        this.codigos = List.copyOf(recorrido);
        this.minutosTotales = minutos;
        this.costoTotal = costo;
    }

    public List<String> getCodigos() {
        return codigos;
    }

    public List<Ruta> getTramos() {
        return tramos;
    }

    public int getCantidadVuelos() {
        return tramos.size();
    }

    public long getMinutosTotales() {
        return minutosTotales;
    }

    public long getCostoTotal() {
        return costoTotal;
    }
}
