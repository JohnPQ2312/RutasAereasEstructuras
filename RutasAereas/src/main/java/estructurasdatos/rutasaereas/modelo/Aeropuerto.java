package estructurasdatos.rutasaereas.modelo;

//Datos de un aeropuerto. Las conexiones se administran en el grafo.
public class Aeropuerto {

    private final String codigo;
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
