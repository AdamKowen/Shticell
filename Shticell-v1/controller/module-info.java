module controller {
    requires javafx.controls;
    requires javafx.fxml;
    requires engine;
    exports sheetController;  // מייצא את חבילת ה-controllers לשאר המודולים
    exports fileController;
}
