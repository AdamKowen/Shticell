package dto;

import sheet.coordinate.api.Coordinate;

import java.util.List;
import java.util.Map;

public interface SheetDto {

    CellDto getCell(int row, int column);

    // Getters
    public int getNumOfColumns();

    public int getNumOfRows();

    public int getVersion();

    public String getName();

    public Integer getColumnUnits();

    public Integer getRowUnits();

    public void setRowUnits(Integer rowUnits);

    // Function to get a cell from the sheet
    public CellDto getCell(Coordinate coordinate);

    // Function to get the current state of the sheet
    public Map<Coordinate, CellDto> getSheet();


    List<Integer> sortRowsByColumns(List<Integer> rows, List<Character> columnChars);

    public List<Integer> resetSoretedOrder();

    Map<String, RangeDto>  getRanges();

    public Map<String, List<String>> getUniqueValuesInRange(List<Integer> rows, List<String> columns);

}
