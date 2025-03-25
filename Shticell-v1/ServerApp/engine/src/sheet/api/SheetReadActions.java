package sheet.api;

import dto.SheetDto;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;
import sheet.range.api.Range;

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


    SheetDto getVersionDto(int version);

    boolean isCoordinateInRange(Coordinate coordinate);

    boolean isCellEmpty(Coordinate coordinate);

    List<Cell> getCellsInRange(String name);

    Map<String, Range> getRanges();

    List<Integer> getNumCellChangedHistory();
}