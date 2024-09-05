package dto;

import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;

import java.util.HashMap;
import java.util.Map;

public class SheetDtoImpl implements SheetDto{
    private int numOfColumns;
    private int numOfRows;
    private int version;
    private String name;
    private Integer columnUnits;
    private Integer rowUnits;
    private Map<Coordinate, CellDto> cellsInSheet;

    public SheetDtoImpl(Sheet sheet){
        this.numOfColumns = sheet.getNumOfColumns();
        this.numOfRows = sheet.getNumOfRows();
        this.version = sheet.getVersion();
        this.name = sheet.getName();
        this.columnUnits = sheet.getColumnUnits();
        this.rowUnits = sheet.getRowUnits();

        Map<Coordinate, Cell> currSheet = sheet.getSheet();
        Map<Coordinate, CellDto> currSheetToDto = new HashMap<>();

        for (Map.Entry<Coordinate, Cell> entry : currSheet.entrySet()) {
            Coordinate coordinate = entry.getKey(); // מפתח (קואורדינטות התא)
            Cell cell = entry.getValue(); // ערך (אובייקט תא)

            // יצירת CellDto מהאובייקט Cell
            CellDto cellDto = new CellDtoImpl(cell);

            // הכנסת ה-CellDto למפה החדשה
            currSheetToDto.put(coordinate, cellDto);
        }

        this.cellsInSheet = currSheetToDto;
    }





    @Override
    public CellDto getCell(int row, int column) {
        return cellsInSheet.get(CoordinateCache.createCoordinate(row, column));
    }


    @Override
    // Getters and Setters
    public int getNumOfColumns() {
        return numOfColumns;
    }

    @Override
    public int getNumOfRows() {
        return numOfRows;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getColumnUnits() {
        return columnUnits;
    }

    @Override
    public Integer getRowUnits() {
        return rowUnits;
    }

    @Override
    public void setRowUnits(Integer rowUnits) {
        this.rowUnits = rowUnits;
    }

    @Override
    // Function to get a cell from the sheet
    public CellDto getCell(Coordinate coordinate) {
        return cellsInSheet.get(coordinate);
    }

    @Override
    // Function to get the current state of the sheet
    public Map<Coordinate, CellDto> getSheet() {
        return cellsInSheet;
    }

}
