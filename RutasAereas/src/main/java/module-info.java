module estructurasdatos.rutasaereas {
    requires javafx.controls;
    requires javafx.fxml;

    opens estructurasdatos.rutasaereas.controlador to javafx.fxml;
    exports estructurasdatos.rutasaereas;
}
