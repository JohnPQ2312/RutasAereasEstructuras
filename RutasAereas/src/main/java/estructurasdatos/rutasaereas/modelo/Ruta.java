package estructurasdatos.rutasaereas.modelo;

// Datos de una conexión dirigida; el origen lo determina la lista que la contiene.
// Sus valores se conservan aunque se retire la conexión del grafo activo.
public class Ruta {

    private final String destino;
    // Pesos completos del vuelo, expresados en minutos y dólares.
    private final int minutos;
    private final int costo;

    public Ruta(String destino, int minutos, int costo) {
        this.destino = destino;
        this.minutos = minutos;
        this.costo = costo;
    }

    public String getDestino() {
        return destino;
    }

    public int getMinutos() {
        return minutos;
    }

    public int getCosto() {
        return costo;
    }
}
