package dto;

import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import sheet.range.api.Range;

import java.util.*;
import java.util.stream.Collectors;

public class SheetDtoImpl implements SheetDto{
    private int numOfColumns;
    private int numOfRows;
    private int version;
    private String name;
    private Integer columnUnits;
    private Integer rowUnits;
    private Map<Coordinate, CellDto> cellsInSheet;
    private List<Integer> numCellChangedHistory;
    private Map<String, RangeDto> ranges ;

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
            Coordinate coordinate = entry.getKey(); // searches for coordinated
            Cell cell = entry.getValue(); // gets cell

            // creating cell dto
            CellDto cellDto = new CellDtoImpl(cell);

            // inserting dto to new map
            currSheetToDto.put(coordinate, cellDto);
        }

        this.cellsInSheet = currSheetToDto;



        Map<String, Range> currRanges = sheet.getRanges();
        Map<String, RangeDto> currRangesToDto = new HashMap<>();

        for (Map.Entry<String, Range> entry : currRanges.entrySet()) {
            String name = entry.getKey(); // key - name
            Range range = entry.getValue(); // value - range

            // created range dto
            RangeDto rangeDto = new RangeDto(range);

            // inserts to map
            currRangesToDto.put(name, rangeDto);
        }

        this.ranges = currRangesToDto;


        this.numCellChangedHistory = sheet.getNumCellChangedHistory() != null
                ? new ArrayList<>(sheet.getNumCellChangedHistory())
                : new ArrayList<>();

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


    // function to sort rows according to col list
    public List<Integer> sortRowsByColumns(List<Integer> rows, List<Character> columnChars) {
        // char of cols to index
        List<Integer> columnIndices = convertColumnsToIndices(columnChars);

        // gets the rows in range
        Map<Integer, List<CellDto>> rowsInRange = getRowsInRange(rows);

        // sorting according to selected rows only
        List<Integer> sortedRowNumbers = sortRowsByGivenColumns(rowsInRange, columnIndices);

        return sortedRowNumbers;
    }


    // cols as chars to index
    private List<Integer> convertColumnsToIndices(List<Character> columnChars) {
        return columnChars.stream()
                .map(c -> (int) c - 'A' + 1) // every col to num (starts from A=0)
                .collect(Collectors.toList());
    }

    // returns the rows from sheet according to the list of wanted rows
    private Map<Integer, List<CellDto>> getRowsInRange(List<Integer> rows) {
        Map<Integer, List<CellDto>> rowsMap = new HashMap<>();

        for (int row : rows) {
            List<CellDto> cellsInRow = new ArrayList<>();

            // gets all the cells in current row
            for (int col = 1; col <= numOfColumns; col++) {
                Coordinate coord = CoordinateCache.createCoordinate(row, col);
                if (cellsInSheet.containsKey(coord)) {
                    cellsInRow.add(cellsInSheet.get(coord));
                }
            }

            rowsMap.put(row, cellsInRow);
        }

        return rowsMap;
    }


    private List<Integer> sortRowsByGivenColumns(Map<Integer, List<CellDto>> rowsInRange, List<Integer> columnIndices) {

        // sorting rows from row list
        List<Integer> sortedRowNumbers = rowsInRange.keySet().stream()
                .sorted((row1, row2) -> compareRowsByColumns(row1, row2, rowsInRange, columnIndices))
                .collect(Collectors.toList());

        // returns the rows sorted
        return sortedRowNumbers;
    }


    // comapres two row in the order of list of cols
    private int compareRowsByColumns(int row1, int row2, Map<Integer, List<CellDto>> rowsInRange, List<Integer> columnIndices) {
        //moving according the cols cell cy cell
        for (Integer columnIndex : columnIndices) {
            CellDto cell1 = getCellInRow(row1, columnIndex, rowsInRange);
            CellDto cell2 = getCellInRow(row2, columnIndex, rowsInRange);

            // compares
            int comparison = compareCells(cell1, cell2);
            if (comparison != 0) {
                return comparison; // if the return value has an answer (values differ) then returns -1 or 1 accordingly
            }
        }
        return 0; // if all the values are the same so it wont change the order of rows - return 0
    }


    // gets cell in row according to col
    private CellDto getCellInRow(int row, int column, Map<Integer, List<CellDto>> rowsInRange) {
        List<CellDto> rowCells = rowsInRange.get(row);

        // looks for the index
        for (CellDto cell : rowCells) {
            if (cell.getCoordinate().getColumn() == column) {
                return cell;
            }
        }

        // returns null if cell wasnt found
        return null;
    }


    // compares cells according to value
    private int compareCells(CellDto cell1, CellDto cell2) {
        if (cell1 == null && cell2 == null) {
            return 0; // both empty - no change
        } else if (cell1 == null) {
            return -1; // cell 1 empty will be considered smaller than value
        } else if (cell2 == null) {
            return 1;  // cell 2 empty will be favoring cell 1
        }

        // according to num' ignores non numeric values
        try {
            double value1 = Double.parseDouble(cell1.getValue());
            double value2 = Double.parseDouble(cell2.getValue());
            return Double.compare(value1, value2);
        } catch (NumberFormatException e) {
            return 0; // if not a number, cant be comapred
        }
    }


    public List<Integer> resetSoretedOrder() {
        List<Integer> order = new ArrayList<>();
        for(int i = 1; i <= numOfRows; i++)
        {
            order.add(i);
        }
        return order;
    }


    public Map<String, RangeDto>  getRanges() {
        return ranges;
    }


    // uses set to keep unique values
    public Map<String, List<String>> getUniqueValuesInRange(List<Integer> rows, List<String> columns) {
        Map<String, Set<String>> uniqueValuesMap = new HashMap<>(); // uses set to keep unique values

        // runs on all the rows in range
        for (int row : rows) {
            // all cols on range
            for (String colName : columns) {
                int col = convertColumnStringToNumber(colName); // col name to index

                Coordinate coordinate = CoordinateCache.createCoordinate(row, col); // coor for cell
                CellDto cell = cellsInSheet.get(coordinate);

                if (cell != null) {
                    String cellValue = cell.getValue();

                    // adding value
                    uniqueValuesMap.putIfAbsent(colName, new HashSet<>());
                    uniqueValuesMap.get(colName).add(cellValue);
                }
            }
        }

        // converts set to list to get the desired resoult
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : uniqueValuesMap.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        return result;
    }

    // col from string to num
    private int convertColumnStringToNumber(String columnString) {
        int columnNumber = 0;
        for (int i = 0; i < columnString.length(); i++) {
            char c = Character.toUpperCase(columnString.charAt(i));
            columnNumber = columnNumber * 26 + (c - 'A' + 1);
        }
        return columnNumber;
    }


    public List<Integer> getNumCellChangedHistory() {
        return numCellChangedHistory;
    }

}
