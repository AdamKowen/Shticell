package controllerPack;

import dto.CellDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sheet.coordinate.api.Coordinate;

public class UImodel {

    private final Map<Coordinate, StringProperty> cellProperties = new HashMap<>();

    public UImodel() {
    }

    // קבלת ה-StringProperty של תא לפי מיקום, אם התא לא קיים, נוצר חדש אוטומטית
    public StringProperty getCellProperty(Coordinate coordinate) {
        return cellProperties.computeIfAbsent(coordinate, k -> new SimpleStringProperty(""));
    }

    // עדכון תא
    public void updateCell(Coordinate coordinate, String value) {
        getCellProperty(coordinate).set(value);
    }

}



