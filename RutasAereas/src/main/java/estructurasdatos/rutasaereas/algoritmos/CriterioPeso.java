package estructurasdatos.rutasaereas.algoritmos;

import estructurasdatos.rutasaereas.modelo.Ruta;

// Permite elegir minutos o costo en el mismo algoritmo de Dijkstra.
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
