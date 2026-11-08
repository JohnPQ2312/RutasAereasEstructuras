# Rutas aéreas

Proyecto II del curso EIF207 Estructuras de Datos. La aplicación carga una red de aeropuertos y permite consultar recorridos con menos vuelos, menor duración o menor costo. También muestra los destinos alcanzables, permite cancelar y restaurar conexiones y reproduce el viaje sobre un mapa.

## Requisitos

- JDK 26. El proyecto está configurado para compilar con Java 26.
- JavaFX 26.0.1, administrado mediante Maven.
- Apache Maven 3. La versión utilizada fue 3.9.15.
- Entorno utilizado: Apache NetBeans IDE 30 en Windows 11, con JDK 26.0.2.

Maven descarga las dependencias declaradas en `pom.xml`. La primera ejecución necesita conexión a Internet si todavía no están descargadas. En NetBeans puede utilizarse el Maven incluido con el IDE.

## Compilación y ejecución

### Desde NetBeans

1. Abrir como proyecto la carpeta `RutasAereas`, que contiene `pom.xml`.
2. Seleccionar JDK 26 como plataforma del proyecto y permitir que Maven cargue las dependencias.
3. Utilizar la acción **Ejecutar proyecto**.

### Desde una terminal

Situarse en la carpeta `RutasAereas`, donde están `pom.xml` y `datos`. Comprobar con `mvn -version` que Maven utiliza Java 26. El comando `mvn` debe estar disponible en la terminal.

Compilar:

```text
mvn compile
```

Ejecutar:

```text
mvn javafx:run
```

Los archivos de datos se buscan desde la carpeta de ejecución. Por eso, los comandos deben ejecutarse desde `RutasAereas`.

## Datos de entrada

La carpeta `datos` contiene los archivos asignados, con 40 aeropuertos y 202 rutas:

- `aeropuertos.txt`: comienza con la cantidad de aeropuertos. Cada registro contiene `codigo x y ciudad pais`.
- `rutas.txt`: comienza con la cantidad de rutas. Cada registro contiene `origen destino minutos costo`.

Los campos se separan por espacios. Los nombres de ciudades y países utilizan guiones bajos en lugar de espacios. Los códigos deben escribirse en mayúsculas para coincidir con las consultas de la interfaz. Las coordenadas se interpretan sobre una referencia de 1000 × 1000. Los minutos y costos son enteros no negativos.

Cada registro de ruta indica una conexión dirigida. Para tener conexión en ambos sentidos se necesitan dos registros. Las cantidades iniciales deben coincidir con los registros del archivo; el lector no admite encabezados ni comentarios adicionales.

Para cargar otros datos, cerrar la aplicación, sustituir ambos archivos de `datos` y volver a ejecutarla. Las cantidades se leen desde los archivos.

## Uso de la aplicación

- **Viaje:** escribir origen y destino, seleccionar el criterio y pulsar **Buscar recorrido**. El resultado muestra códigos, vuelos, minutos y costo, y comienza la animación.
- **Aeropuerto:** consultar un código para ver sus datos y conexiones salientes activas.
- **Red:** consultar los destinos alcanzables desde el origen, cancelar una conexión directa o restaurar todas las rutas. Utiliza los campos de origen y destino situados sobre las pestañas.
- **Mapa:** la rueda amplía la vista, el arrastre la desplaza y **Restablecer vista** recupera el encuadre inicial.
- **Vuelo:** **Pausar / continuar** controla la reproducción y **Reiniciar vuelo** comienza otra vez desde el origen.

Cancelar afecta únicamente las conexiones directas del origen al destino indicado. Para un viaje con escalas debe indicarse uno de sus tramos. Los cambios se realizan en memoria y los archivos de entrada se conservan. **Restaurar todas las rutas** recupera las conexiones canceladas.

## Organización y documentación

El código Java se divide en los paquetes `modelo`, `persistencia`, `algoritmos`, `controlador` y `vista`. Los archivos FXML y CSS están en `src/main/resources`.

En la carpeta superior a `RutasAereas` se encuentran:

- `Documentacion`: informe en PDF y documento editable.
- `Pruebas`: conjuntos de datos válidos e inválidos, guía de reproducción y resultados de las verificaciones.

BFS, DFS y Dijkstra están implementados en `BuscadorRutas` mediante colecciones estándar de Java. Dijkstra utiliza el criterio seleccionado para trabajar con minutos o costo.

Durante el desarrollo se utilizó Codex (OpenAI) para generar y revisar código, comentarios, documentación y pruebas. El estudiante dirigió el trabajo, propuso ajustes y participó en la revisión de los resultados y las decisiones del proyecto.
