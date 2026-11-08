package estructurasdatos.rutasaereas.modelo;

// Datos de un vértice. Sus conexiones se administran por separado en Grafo.
public class Aeropuerto {

    private final String codigo;
    // Coordenadas del archivo: X hacia la derecha, Y hacia abajo.
    private final int x;
    private final int y;
    private final String ciudad;
    private final String pais;

    public Aeropuerto(String codigo, int x, int y,
            String ciudad, String pais) {
        this.codigo = codigo;
        this.x = x;
        this.y = y;
        this.ciudad = ciudad;
        this.pais = pais;
    }

    // Consultas de lectura; los datos del aeropuerto no cambian al dibujarlo.
    public String getCodigo() {
        return codigo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getPais() {
        return pais;
    }
}
