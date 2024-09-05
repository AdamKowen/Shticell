package sheet.api;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;


public interface SheetUpdateActions {


    void setCell(int row, int column, String value);

    void setNumOfColumns(int numOfColumns);

    void setNumOfRows(int numOfRows);

    void setName(String name);

    void setColumnUnits(Integer columnUnits);

    void setRowUnits(Integer rowUnits);

    // Function to add or update a cell in the sheet
    void setCell(Coordinate coordinate, Cell cell);

    // Function to remove a cell from the sheet
    void removeCell(Coordinate coordinate);

    // Function to clear the sheet
    void clearSheet();

    void saveVersion();

    // Function to update the version of the sheet
    void updateVersion();

}
