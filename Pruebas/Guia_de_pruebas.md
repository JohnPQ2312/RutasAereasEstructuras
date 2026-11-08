# Casos de prueba de Rutas Aéreas

Este documento presenta los conjuntos de entrada, el procedimiento de reproducción y los resultados esperados para evaluar el programa con datos distintos de los asignados. Los aeropuertos y vuelos de prueba son ficticios. Los archivos conservan el formato del enunciado: cantidad inicial de registros y filas de datos.

## Procedimiento de reproducción

1. Trabajar sobre una copia de la carpeta RutasAereas y conservar los datos asignados en el proyecto original.
2. Cerrar la aplicación de esa copia, elegir un conjunto de Datos_validos y copiar juntos sus archivos aeropuertos.txt y rutas.txt a la carpeta datos de la copia.
3. Ejecutar la aplicación desde la carpeta del proyecto Maven de esa copia, que contiene pom.xml y datos. Los archivos se leen al iniciar; cambiar los archivos con la ventana abierta no recarga el grafo.
4. Revisar los contadores, las consultas siguientes, DFS, mapa y animación. Probar también código XXX, campos vacíos y códigos escritos en minúscula.
5. Para pasar al siguiente conjunto, cerrar la aplicación y repetir con ambos archivos del nuevo conjunto.

No mezclar aeropuertos de un conjunto con rutas de otro. Las ciudades y países con varias palabras usan guion bajo. Las coordenadas están dentro de 0 a 1000, con el origen arriba a la izquierda.

## Cómo interpretar los resultados

BFS minimiza la cantidad de vuelos; MINUTOS y COSTO son los dos criterios de Dijkstra. Cuando hay empates, el programa puede elegir cualquiera de los recorridos óptimos. Las tablas muestran un ejemplo válido; los vuelos y los otros totales pueden cambiar si el programa elige otra ruta empatada. Los datos no pretenden exigir un desempate específico.

## Ubicación de los controles

Los campos **Origen** y **Destino** permanecen arriba del panel izquierdo. Las acciones se encuentran en estas pestañas:

- **Viaje:** seleccionar Menos vuelos, Menor tiempo o Menor costo y pulsar Buscar recorrido.
- **Aeropuerto:** escribir el código de un aeropuerto y pulsar Consultar aeropuerto.
- **Red:** consultar los destinos desde el origen, cancelar una conexión directa o restaurar todas las rutas.

Consultar destinos solo necesita el origen. Cancelar utiliza origen y destino y afecta una conexión directa, no un itinerario completo con escalas. Cambiar de pestaña conserva los campos; no ejecuta una consulta ni modifica el grafo. El resultado y los controles Pausar / continuar y Reiniciar vuelo aparecen debajo. Restablecer vista se encuentra sobre el mapa.

En las tablas, **BFS** corresponde a Menos vuelos; **MINUTOS**, a Menor tiempo; y **COSTO**, a Menor costo.

## Conjuntos válidos

| Carpeta | Aeropuertos | Rutas | Objetivo |
|---|---:|---:|---|
| 01_criterios_distintos | 6 | 10 | Los tres criterios dan resultados distintos |
| 02_sentidos_ciclos_componentes | 7 | 8 | Direcciones, ciclos, componentes y aeropuerto sin salidas |
| 03_cancelacion_puente | 6 | 7 | Cancelación con alternativa y cancelación que aisla destinos |
| 04_pesos_cero_empates | 5 | 8 | Pesos cero, ciclo de costo cero y empates |
| 05_aeropuerto_unico | 1 | 0 | Un aeropuerto y ninguna ruta |
| 06_mapa_limites | 6 | 6 | Esquinas del mapa y dos aeropuertos cercanos |
| 07_cadena_larga | 12 | 11 | Itinerario de once vuelos |
| 08_varios_sin_rutas | 3 | 0 | Varios aeropuertos aislados |

### 01_criterios_distintos

Desde AAA todos los otros cinco aeropuertos son alcanzables. AAA -> FFF debe ser directo con BFS, pasar por BBB al minimizar minutos y pasar por CCC y DDD al minimizar costo.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| AAA -> FFF | BFS | AAA -> FFF | 1 | 300 | $900 |
| AAA -> FFF | MINUTOS | AAA -> BBB -> FFF | 2 | 60 | $800 |
| AAA -> FFF | COSTO | AAA -> CCC -> DDD -> FFF | 3 | 180 | $150 |
| FFF -> AAA | BFS | FFF -> AAA | 1 | 350 | $1000 |
| FFF -> AAA | MINUTOS | FFF -> AAA | 1 | 350 | $1000 |
| FFF -> AAA | COSTO | FFF -> AAA | 1 | 350 | $1000 |
| AAA -> AAA | BFS | AAA | 0 | 0 | $0 |
| AAA -> AAA | MINUTOS | AAA | 0 | 0 | $0 |
| AAA -> AAA | COSTO | AAA | 0 | 0 | $0 |

### 02_sentidos_ciclos_componentes

Desde NTE hay tres destinos alcanzables (EST, FIN, SUR) y tres inalcanzables (ISL, OES, SOL). FIN no tiene salidas. OES llega a NTE, pero NTE no llega a OES. Los ciclos no deben provocar repeticiones indefinidas.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| NTE -> FIN | BFS | NTE -> EST -> FIN | 2 | 25 | $120 |
| NTE -> FIN | MINUTOS | NTE -> EST -> FIN | 2 | 25 | $120 |
| NTE -> FIN | COSTO | NTE -> EST -> FIN | 2 | 25 | $120 |
| FIN -> NTE | BFS | Sin recorrido | - | - | - |
| FIN -> NTE | MINUTOS | Sin recorrido | - | - | - |
| FIN -> NTE | COSTO | Sin recorrido | - | - | - |
| NTE -> OES | BFS | Sin recorrido | - | - | - |
| NTE -> OES | MINUTOS | Sin recorrido | - | - | - |
| NTE -> OES | COSTO | Sin recorrido | - | - | - |
| ISL -> SOL | BFS | ISL -> SOL | 1 | 5 | $7 |
| ISL -> SOL | MINUTOS | ISL -> SOL | 1 | 5 | $7 |
| ISL -> SOL | COSTO | ISL -> SOL | 1 | 5 | $7 |

### 03_cancelacion_puente

Inicialmente hay siete conexiones. Desde PUE hay cuatro destinos alcanzables (ALT, FIN, HUB, RAM) y solo ISO es inalcanzable. Ver la secuencia de cancelaciones al final de esta guía.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| PUE -> FIN | BFS | PUE -> HUB -> RAM -> FIN | 3 | 120 | $150 |
| PUE -> FIN | MINUTOS | PUE -> ALT -> HUB -> RAM -> FIN | 4 | 110 | $70 |
| PUE -> FIN | COSTO | PUE -> ALT -> HUB -> RAM -> FIN | 4 | 110 | $70 |
| FIN -> PUE | BFS | Sin recorrido | - | - | - |
| FIN -> PUE | MINUTOS | Sin recorrido | - | - | - |
| FIN -> PUE | COSTO | Sin recorrido | - | - | - |
| ISO -> PUE | BFS | Sin recorrido | - | - | - |
| ISO -> PUE | MINUTOS | Sin recorrido | - | - | - |
| ISO -> PUE | COSTO | Sin recorrido | - | - | - |

### 04_pesos_cero_empates

Prueba adicional de pesos no negativos. Desde ZER todos los otros cuatro aeropuertos son alcanzables. Hay varios recorridos óptimos hacia FIN: aceptar los empates segun el criterio elegido. Un tramo con cero minutos o costo debe seguir dibujandose y animandose.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| ZER -> FIN | BFS | ZER -> UNO -> FIN | 2 | 5 | $5 |
| ZER -> FIN | MINUTOS | ZER -> UNO -> TRE -> FIN | 3 | 5 | $5 |
| ZER -> FIN | COSTO | ZER -> UNO -> TRE -> FIN | 3 | 5 | $5 |
| ZER -> UNO | BFS | ZER -> UNO | 1 | 0 | $0 |
| ZER -> UNO | MINUTOS | ZER -> UNO | 1 | 0 | $0 |
| ZER -> UNO | COSTO | ZER -> UNO | 1 | 0 | $0 |
| FIN -> ZER | BFS | Sin recorrido | - | - | - |
| FIN -> ZER | MINUTOS | Sin recorrido | - | - | - |
| FIN -> ZER | COSTO | Sin recorrido | - | - | - |

### 05_aeropuerto_unico

Debe aparecer un solo punto. UNQ no tiene salidas. DFS muestra cero destinos alcanzables y cero inalcanzables. Origen igual al destino: cero vuelos, cero minutos y costo cero.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| UNQ -> UNQ | BFS | UNQ | 0 | 0 | $0 |
| UNQ -> UNQ | MINUTOS | UNQ | 0 | 0 | $0 |
| UNQ -> UNQ | COSTO | UNQ | 0 | 0 | $0 |

### 06_mapa_limites

Todos los aeropuertos se alcanzan entre sí siguiendo el ciclo. Comprobar puntos y etiquetas de las cuatro esquinas, zoom, desplazamiento y orientación del avion en distintos sentidos. CES y CEN están separados por veinte unidades.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| NWN -> SEN | BFS | NWN -> NEN -> CEN -> CES -> SWN -> SEN | 5 | 100 | $140 |
| NWN -> SEN | MINUTOS | NWN -> NEN -> CEN -> CES -> SWN -> SEN | 5 | 100 | $140 |
| NWN -> SEN | COSTO | NWN -> NEN -> CEN -> CES -> SWN -> SEN | 5 | 100 | $140 |
| CEN -> CES | BFS | CEN -> CES | 1 | 0 | $0 |
| CEN -> CES | MINUTOS | CEN -> CES | 1 | 0 | $0 |
| CEN -> CES | COSTO | CEN -> CES | 1 | 0 | $0 |
| SEN -> NEN | BFS | SEN -> NWN -> NEN | 2 | 60 | $80 |
| SEN -> NEN | MINUTOS | SEN -> NWN -> NEN | 2 | 60 | $80 |
| SEN -> NEN | COSTO | SEN -> NWN -> NEN | 2 | 60 | $80 |

### 07_cadena_larga

CAA -> CAL tiene once vuelos, 660 minutos y costo 330. La animación completa dura aproximadamente 43 segundos: once vuelos de tres segundos y diez escalas de un segundo. Desde CAA hay once destinos alcanzables; desde CAL ninguno.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| CAA -> CAL | BFS | CAA -> CAB -> CAC -> CAD -> CAE -> CAF -> CAG -> CAH -> CAI -> CAJ -> CAK -> CAL | 11 | 660 | $330 |
| CAA -> CAL | MINUTOS | CAA -> CAB -> CAC -> CAD -> CAE -> CAF -> CAG -> CAH -> CAI -> CAJ -> CAK -> CAL | 11 | 660 | $330 |
| CAA -> CAL | COSTO | CAA -> CAB -> CAC -> CAD -> CAE -> CAF -> CAG -> CAH -> CAI -> CAJ -> CAK -> CAL | 11 | 660 | $330 |
| CAL -> CAA | BFS | Sin recorrido | - | - | - |
| CAL -> CAA | MINUTOS | Sin recorrido | - | - | - |
| CAL -> CAA | COSTO | Sin recorrido | - | - | - |
| CAF -> CAL | BFS | CAF -> CAG -> CAH -> CAI -> CAJ -> CAK -> CAL | 6 | 510 | $255 |
| CAF -> CAL | MINUTOS | CAF -> CAG -> CAH -> CAI -> CAJ -> CAK -> CAL | 6 | 510 | $255 |
| CAF -> CAL | COSTO | CAF -> CAG -> CAH -> CAI -> CAJ -> CAK -> CAL | 6 | 510 | $255 |

### 08_varios_sin_rutas

Tres puntos y ninguna conexión. Desde cualquiera hay cero destinos alcanzables y dos inalcanzables. Solo hay recorrido cuando origen y destino coinciden.

| Origen -> destino | Criterio | Ejemplo de recorrido | Vuelos | Minutos | Costo |
|---|---|---|---:|---:|---:|
| WES -> EAS | BFS | Sin recorrido | - | - | - |
| WES -> EAS | MINUTOS | Sin recorrido | - | - | - |
| WES -> EAS | COSTO | Sin recorrido | - | - | - |
| MID -> MID | BFS | MID | 0 | 0 | $0 |
| MID -> MID | MINUTOS | MID | 0 | 0 | $0 |
| MID -> MID | COSTO | MID | 0 | 0 | $0 |

## Secuencia de cancelación y restauración

Usar 03_cancelacion_puente. Ejecutar cada búsqueda con los tres criterios y comprobar también DFS.

| Paso | Accion | Resultado esperado |
|---:|---|---|
| 1 | Consultar PUE -> FIN | BFS: PUE -> HUB -> RAM -> FIN, 3 vuelos, 120 min, $150. Dijkstra por minutos y costo: PUE -> ALT -> HUB -> RAM -> FIN, 4 vuelos, 110 min, $70. Contador: 7 rutas. |
| 2 | Cancelar PUE -> HUB | Contador: 6. FIN sigue siendo alcanzable por ALT. Ahora los tres criterios usan PUE -> ALT -> HUB -> RAM -> FIN. |
| 3 | Restaurar todas las rutas | Se restaura una; contador: 7. |
| 4 | Cancelar HUB -> RAM | Contador: 6. PUE -> FIN y PUE -> RAM dejan de existir. Desde PUE: 2 destinos alcanzables (ALT, HUB), 3 inalcanzables (FIN, ISO, RAM). RAM -> HUB sigue activa: 35 min, $25. |
| 5 | Repetir la cancelación HUB -> RAM | No hay conexión activa que cancelar. Contador: 6. |
| 6 | Restaurar todas las rutas | Se restaura una; contador: 7. PUE -> FIN vuelve a existir. |
| 7 | Restaurar otra vez | No hay rutas canceladas; contador: 7. |
| 8 | Cancelar PUE -> FIN | No hay conexión directa; contador: 7. El itinerario con escalas sigue existiendo. |

## Archivos con errores intencionales

Estos conjuntos contienen errores intencionales y deben ser rechazados al cargar. Para comprobarlos, usar la misma copia de prueba, cerrar la aplicación y sustituir juntos los dos archivos antes de ejecutar. La carga debe interrumpirse; NetBeans muestra el error de lectura en su panel de salida. No se espera que se abra un mapa con esos datos. Esta comprobación es adicional: un archivo inválido al iniciar es un caso distinto de escribir un código inexistente en la interfaz. Después, volver a colocar un conjunto válido.

| Carpeta | Error esperado |
|---|---|
| 01_codigo_duplicado | Código AAA repetido en aeropuertos. |
| 02_destino_inexistente | CCC no figura en aeropuertos. |
| 03_minutos_negativos | Una ruta tiene minutos negativos. |
| 04_costo_negativo | Una ruta tiene costo negativo. |
| 05_falta_aeropuerto | Se declaran tres aeropuertos, pero solo hay dos filas. |
| 06_falta_ruta | Se declaran dos rutas, pero solo hay una fila. |
| 07_sobra_aeropuerto | Se declara un aeropuerto, pero hay dos filas. |
| 08_sobra_ruta | Se declaran cero rutas, pero hay una fila. |
| 09_minutos_no_numericos | El campo minutos contiene texto. |
| 10_cantidad_negativa | La cantidad declarada de rutas es negativa. |

## Registro e interpretación de la evidencia

Las tablas de esta guía describen resultados esperados; no constituyen por sí solas un registro de ejecución. Una nueva ejecución puede documentarse mediante su fecha, entorno, conjunto de datos, consulta, criterio, resultado observado y diferencias respecto de lo esperado.

Los archivos Resultado_verificacion.txt y Resultado_integracion.txt contienen resúmenes de verificaciones previas realizadas con asistencia de IA y utilidades auxiliares externas al código de la aplicación. Se distingue la comprobación automática de la inspección visual. Esos resultados no se atribuyen a ejecuciones manuales del estudiante.

## Verificaciones realizadas y alcance

- **Carga y lógica:** se comprobaron 1008 consultas con los tres criterios y se compararon con resultados calculados mediante un verificador auxiliar independiente del programa. También se revisaron los tramos y totales, 46 consultas de alcanzabilidad, 50 conexiones canceladas y restauradas por separado y el rechazo de los diez conjuntos inválidos. El resumen está en Resultado_verificacion.txt. El verificador auxiliar no forma parte de los algoritmos de la aplicación.
- **Funcionamiento conjunto de la interfaz:** el 5 de noviembre pasaron 190 comprobaciones automáticas con la vista, el controlador y el animador del programa, usando los ocho conjuntos válidos. Incluyen búsquedas, totales, finalización del vuelo, consulta de aeropuertos, colores de alcanzabilidad, códigos inexistentes y cancelación/restauración del puente. Las reproducciones largas se aceleraron solo durante esas comprobaciones; la aplicación conserva tres segundos por vuelo y un segundo por escala.
- **Inspección visual asistida:** en ese conjunto de pruebas se revisaron en la ventana los conjuntos 01_criterios_distintos y 06_mapa_limites. Se observaron los tres recorridos AAA -> FFF y su animación, los puntos de las cuatro esquinas, las etiquetas cercanas y el vuelo CEN -> CES con cero minutos y costo. Los otros seis conjuntos se comprobaron automáticamente, sin inspección visual individual en ese conjunto de pruebas. La copia de la aplicación compiló y arrancó correctamente. El resumen está en Resultado_integracion.txt.

Los resultados corresponden a ese conjunto de pruebas. Los casos pueden reproducirse desde la aplicación y las nuevas observaciones deben registrarse por separado. No se afirma que todos los pasos de esta guía hayan sido ejecutados mediante inspección visual.
