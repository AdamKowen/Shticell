package sheet.impl;
import dto.CellDto;
import dto.SheetDto;
import dto.SheetDtoImpl;
import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.coordinate.api.*;
import sheet.coordinate.impl.CoordinateCache;
import sheet.range.api.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sheet.coordinate.impl.CoordinateCache.createCoordinate;

public class SheetImpl implements Sheet {

    private int numOfColumns;
    private int numOfRows;
    private int version;
    private String name;
    private Integer columnUnits;
    private Integer rowUnits;
    private Map<Coordinate, Cell> cellsInSheet;
    private Map<Integer, SheetDto> versionHistory;
    private List<Integer> numCellChangedHistory;
    private Map<String, Range> ranges ;


    // Constructor
    public SheetImpl(String name, int numOfColumns, int numOfRows, int columnUnits, int rowUnits, Map<Coordinate, Cell> cellsInSheet,Map<String, Range> rangesInSheet)
    {
        this.name = name;
        this.numOfColumns = numOfColumns;
        this.numOfRows = numOfRows;
        this.version = 0; // Initial Loading version
        this.columnUnits = columnUnits;
        this.rowUnits = rowUnits;
        this.cellsInSheet = cellsInSheet;
        this.versionHistory = new HashMap<>();
        this.numCellChangedHistory = new ArrayList<>();
        this.ranges = rangesInSheet;
    }
    public SheetImpl(){
        this.numOfColumns = 0;
        this.numOfRows = 0;
        this.version = 0;
        this.columnUnits = 0;
        this.rowUnits = 0;
        this.cellsInSheet = new HashMap<>();
        this.versionHistory = new HashMap<>();
        this.numCellChangedHistory = new ArrayList<>();
        this.ranges = new HashMap<>();
    }

    @Override
    public Cell getCell(int row, int column) {
        // checks if in range
        if (row < 1 || row > numOfRows || column < 1 || column > numOfColumns) {
            return null; // not in range
        }

        //
        Coordinate coordinate = createCoordinate(row, column);

        //
        Cell cell = cellsInSheet.get(coordinate);

        // if cell exist
        if (cell == null) {
            return null; // return null if not
        }

        return cell; // return cell
    }


    public void setCell(int row, int column, String value)
    {
        cellsInSheet.get(CoordinateCache.createCoordinate(row,column)).setCellOriginalValue(value, this.version);
    }

    // Getters and Setters
    public int getNumOfColumns() {
        return numOfColumns;
    }

    public void setNumOfColumns(int numOfColumns) {
        this.numOfColumns = numOfColumns;
    }

    public int getNumOfRows() {
        return numOfRows;
    }

    public void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }

    public int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getColumnUnits() {
        return columnUnits;
    }

    public void setColumnUnits(Integer columnUnits) {
        this.columnUnits = columnUnits;
    }

    public Integer getRowUnits() {
        return rowUnits;
    }

    public void setRowUnits(Integer rowUnits) {
        this.rowUnits = rowUnits;
    }

    // Function to add or update a cell in the sheet
    public void setCell(Coordinate coordinate, Cell cell) {
        cellsInSheet.put(coordinate, cell);
    }

    // Function to get a cell from the sheet
    public Cell getCell(Coordinate coordinate) {
        if (isCoordinateInRange(coordinate))
        {
            return cellsInSheet.get(coordinate);
        }
        else
        {
            throw new IllegalArgumentException("Coordinate is not in range");
        }
    }

    // Function to remove a cell from the sheet
    public void removeCell(Coordinate coordinate) {
        cellsInSheet.remove(coordinate);
        updateVersion(); // Update version when modifying the sheet
    }

    // Function to clear the sheet
    public void clearSheet() {
        cellsInSheet.clear();
        updateVersion(); // Update version when clearing the sheet
    }

    // Function to update the version of the sheet
    public void updateVersion() {
        version++;
    }

    // Function to get the current state of the sheet
    public Map<Coordinate, Cell> getSheet() {
        return cellsInSheet;
    }


    @Override
    public Map<String, Range> getRanges() {
        return ranges;
    }


    @Override
    public void saveVersion() {
        updateVersion();
        versionHistory.put(version, new SheetDtoImpl(this));
    }


    @Override
    public List<Integer> countChangedCellsInAllVersions() {
        List<Integer> changesPerVersion = new ArrayList<>();

        // עבור כל גרסה נשמור את מספר התאים שהשתנו
        for (int versionRun = 1; versionRun <= version; versionRun++) {
            SheetDto currentVersionSheet = versionHistory.get(versionRun);

            if (versionRun == 1) {
                // אם זו הגרסה הראשונה, כל התאים נחשבים כמשתנים
                changesPerVersion.add(currentVersionSheet.getSheet().size());
            } else {
                SheetDto previousVersionSheet = versionHistory.get(versionRun - 1);
                Map<Coordinate, CellDto> currentCells = currentVersionSheet.getSheet();
                Map<Coordinate, CellDto> previousCells = previousVersionSheet.getSheet();

                int changedCellsCount = 0;

                for (Map.Entry<Coordinate, CellDto> entry : currentCells.entrySet()) {
                    Coordinate coord = entry.getKey();
                    CellDto currentCell = entry.getValue();
                    CellDto previousCell = previousCells.get(coord);

                    // בדיקה אם הערך האפקטיבי השתנה או שהתא לא היה קיים קודם
                    if (previousCell == null || !currentCell.getValue().equals(previousCell.getValue()) || !currentCell.getOriginalValue().equals(previousCell.getOriginalValue()) ) {
                        changedCellsCount++;
                    }
                }

                changesPerVersion.add(changedCellsCount);
            }
        }

        return changesPerVersion;
    }


    @Override
    public SheetDto getVersionDto(int version)
    {
        return versionHistory.get(version);
    }

    @Override
    public boolean isCoordinateInRange(Coordinate coordinate) {
        int row = coordinate.getRow();
        int column = coordinate.getColumn();

        // בדיקת טווחי השורה והעמודה של הקואורדינטה
        return (row >= 1 && row <= numOfRows && column >= 1 && column <= numOfColumns);
    }


    @Override
    public boolean isCellEmpty(Coordinate coordinate)
    {
        Cell cell = getCell(coordinate);

        // בדיקה אם התא קיים
        if (cell == null) {
            return true; // התא ריק או לא קיים
        }

        return false;
    }

    public void addRange(String name, Range range) {
        if (ranges.containsKey(name)) {
            throw new RuntimeException("Name already in use");
        }
        ranges.put(name, range);
    }


    public Range getRange(String name) {
        return ranges.get(name);
    }

    public void removeRange(String name) {
        if(!checkDeleteRange(name))
            throw new RuntimeException("Range in active use");

        ranges.remove(name);
    }

    public boolean checkDeleteRange(String name) {
        String avgToFind="{AVERAGE,"+name+"}";
        String sumToFind="{SUM,"+name+"}";

        for(Cell cell : cellsInSheet.values()) {
            if(cell.getOriginalValue().contains(avgToFind)||cell.getOriginalValue().contains(sumToFind)) {
                return false;
            }
        }
        return true;
    }



    // Get all cells in the range (returns a list of cells)
    public List<Cell> getCellsInRange(String name) {
        Cell topLeft=getCellByString(ranges.get(name).getBoundaries().getFrom());
        Cell bottomRight= getCellByString(ranges.get(name).getBoundaries().getTo());

        List<Cell> cells = new ArrayList<>();
        for (int row = topLeft.getCoordinate().getRow(); row <= bottomRight.getCoordinate().getRow(); row++) {
            for (int col = topLeft.getCoordinate().getColumn(); col <= bottomRight.getCoordinate().getColumn(); col++) {
                cells.add(this.getCell(row, col));
            }
        }
        return cells;
    }

    private Cell getCellByString(String s) {
        if (s == null || s.length() != 2) {
            throw new IllegalArgumentException("Invalid cell reference: " + s);
        }

        // Extract the column part (letters) and row part (numbers)
        StringBuilder columnPart = new StringBuilder();
        StringBuilder rowPart = new StringBuilder();

        // Separate the column (letters) and row (numbers), can get A1 or 1A
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) {
                columnPart.append(c);
            } else if (Character.isDigit(c)) {
                rowPart.append(c);
            } else {
                throw new IllegalArgumentException("Invalid character in cell reference: " + s);
            }
        }

        if (columnPart.isEmpty() || rowPart.length() == 0) {
            throw new IllegalArgumentException("Invalid cell reference: " + s);
        }

        // Convert column (e.g., 'A') to a column number
        int column = convertColumnStringToNumber(columnPart.toString());

        // Convert row string to integer
        int row = Integer.parseInt(rowPart.toString());

        // Fetch and return the cell using the existing getCell method
        return getCell(row, column);
    }

    // Helper method to convert column string (e.g., "A", "AB") to a column number
    private int convertColumnStringToNumber(String columnString) {
        int columnNumber = 0;
        for (int i = 0; i < columnString.length(); i++) {
            char c = Character.toUpperCase(columnString.charAt(i));
            columnNumber = columnNumber * 26 + (c - 'A' + 1);
        }
        return columnNumber;
    }



}
