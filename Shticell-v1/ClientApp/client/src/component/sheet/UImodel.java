package component.sheet;

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

    // getting StringProperty of cell according to placement. if it doesnt exist, new one is created
    public StringProperty getCellProperty(Coordinate coordinate) {
        return cellProperties.computeIfAbsent(coordinate, k -> new SimpleStringProperty(""));
    }

    // Updating cell
    public void updateCell(Coordinate coordinate, String value) {
        getCellProperty(coordinate).set(value);
    }

}



