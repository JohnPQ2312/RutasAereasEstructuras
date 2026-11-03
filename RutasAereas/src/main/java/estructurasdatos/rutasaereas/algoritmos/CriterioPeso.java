package estructurasdatos.rutasaereas.algoritmos;

import estructurasdatos.rutasaereas.modelo.Ruta;

public enum CriterioPeso {

    MINUTOS,
    COSTO;

    public int obtenerPeso(Ruta ruta) {
        if (this == MINUTOS) {
            return ruta.getMinutos();
        }

        return ruta.getCosto();
    }
}
