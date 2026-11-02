package estructurasdatos.rutasaereas.modelo;

import java.util.ArrayList;
import java.util.List;

public class ResultadoRecorrido {

    private final List<String> codigos;
    private final List<Ruta> tramos;
    private final long minutosTotales;
    private final long costoTotal;

    public ResultadoRecorrido(String origen, List<Ruta> tramos) {
        // Conserva las conexiones elegidas sin permitir cambios en la lista.
        this.tramos = List.copyOf(tramos);

        List<String> recorrido = new ArrayList<>();
        recorrido.add(origen);

        long minutos = 0;
        long costo = 0;

        // Cada conexion agrega un destino y sus dos pesos al resultado.
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