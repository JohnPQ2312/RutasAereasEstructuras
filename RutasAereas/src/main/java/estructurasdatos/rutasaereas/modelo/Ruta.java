package estructurasdatos.rutasaereas.modelo;

// El origen lo determina la lista de adyacencia que contiene esta ruta.
public class Ruta {

    private final String destino;
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
