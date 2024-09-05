package sheet.api;

import dto.SheetDto;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;

import java.util.List;
import java.util.Map;

public interface SheetReadActions {

    Cell getCell(int row, int column);

    // Getters and Setters
    int getNumOfColumns();

    int getNumOfRows();

    int getVersion();

    String getName();

    Integer getColumnUnits();

    Integer getRowUnits();

    // Function to get a cell from the sheet
    Cell getCell(Coordinate coordinate);

    // Function to get the current state of the sheet
    Map<Coordinate, Cell> getSheet();

    List<Integer> countChangedCellsInAllVersions();

    SheetDto getVersionDto(int version);

    boolean isCoordinateInRange(Coordinate coordinate);

    boolean isCellEmpty(Coordinate coordinate);

}