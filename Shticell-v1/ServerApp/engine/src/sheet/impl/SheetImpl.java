package sheet.impl;
import dto.CellDto;
import dto.SheetDto;
import dto.SheetDtoImpl;
import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.coordinate.api.*;
import sheet.coordinate.impl.CoordinateCache;
import sheet.range.api.Range;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static sheet.coordinate.impl.CoordinateCache.createCoordinate;

public class SheetImpl implements Sheet, Serializable {

    private final Lock sheetLock = new ReentrantLock();

    private int numOfColumns;
    private int numOfRows;
    private int version;
    private String name;
    private Integer columnUnits;
    private Integer rowUnits;
    private Map<Coordinate, Cell> cellsInSheet = null;
    private transient Map<Integer, SheetDto> versionHistory = null;
    private transient List<Integer> numCellChangedHistory = null;
    private Map<String, Range> ranges = null;


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
        sheetLock.lock();  // נעילת תור
        try {
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
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
    }


    public void setCell(int row, int column, String value)
    {
        sheetLock.lock();  // נעילת תור
        try {
            cellsInSheet.get(CoordinateCache.createCoordinate(row,column)).setCellOriginalValue(value, this.version);
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
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
        sheetLock.lock();
        try {
            return version;
        } finally {
            sheetLock.unlock();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        sheetLock.lock();
        try {
            this.name = name; //locking in case in the future name change will be possible
        } finally {
            sheetLock.unlock();
        }
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
        sheetLock.lock();
        try {
            cellsInSheet.put(coordinate, cell);
        } finally {
            sheetLock.unlock();
        }
    }

    // Function to get a cell from the sheet
    public Cell getCell(Coordinate coordinate) {

        sheetLock.lock();
        try {
            if (isCoordinateInRange(coordinate))
            {
                return cellsInSheet.get(coordinate);
            }
            else
            {
                throw new IllegalArgumentException("Coordinate is not in range");
            }
        } finally {
            sheetLock.unlock();
        }
    }

    // Function to remove a cell from the sheet
    public void removeCell(Coordinate coordinate) {
        sheetLock.lock();
        try {
            cellsInSheet.remove(coordinate);
            updateVersion(); // Update version when modifying the sheet
        } finally {
            sheetLock.unlock();
        }
    }

    // Function to clear the sheet
    public void clearSheet() {

        sheetLock.lock();
        try {
            cellsInSheet.clear();
            updateVersion(); // Update version when clearing the sheet
        } finally {
            sheetLock.unlock();
        }

    }

    // Function to update the version of the sheet
    public void updateVersion() {
        sheetLock.lock();
        try {
            version++;
        } finally {
            sheetLock.unlock();
        }
    }

    // Function to get the current state of the sheet
    public Map<Coordinate, Cell> getSheet() {

        sheetLock.lock();
        try {
            return cellsInSheet;
        } finally {
            sheetLock.unlock();
        }
    }


    @Override
    public Map<String, Range> getRanges() {

        sheetLock.lock();
        try {
            return ranges;
        } finally {
            sheetLock.unlock();
        }

    }


    @Override
    public void saveVersion() {

        sheetLock.lock();
        try {
            updateVersion(); // עדכון מספר הגרסה
            SheetDto newVersionSheet = new SheetDtoImpl(this); // יצירת ה-SheetDto של הגרסה הנוכחית
            versionHistory.put(version, newVersionSheet); // שמירת הגרסה הנוכחית בגרסת ההיסטוריה

            // חישוב והוספת מספר השינויים בין הגרסה החדשה לבין הקודמת (אם זו לא הגרסה הראשונה)
            if (version == 1) {
                // אם זו הגרסה הראשונה, כל התאים נחשבים כמשתנים
                numCellChangedHistory.add(newVersionSheet.getSheet().size());
            } else {
                // השוואה מול הגרסה הקודמת (version - 1)
                SheetDto previousVersionSheet = versionHistory.get(version - 1);
                int changedCellsCount = countChangedCells(previousVersionSheet, newVersionSheet);
                numCellChangedHistory.add(changedCellsCount);
            }
        } finally {
            sheetLock.unlock();
        }
    }


    private int countChangedCells(SheetDto previousVersion, SheetDto currentVersion) {
        sheetLock.lock();
        try {
            Map<Coordinate, CellDto> previousCells = previousVersion.getSheet();
            Map<Coordinate, CellDto> currentCells = currentVersion.getSheet();

            int changedCellsCount = 0;

            for (Map.Entry<Coordinate, CellDto> entry : currentCells.entrySet()) {
                Coordinate coord = entry.getKey();
                CellDto currentCell = entry.getValue();
                CellDto previousCell = previousCells.get(coord);

                // בדיקה אם הערך האפקטיבי השתנה או שהתא לא היה קיים קודם
                boolean valueChanged = previousCell == null ||
                        !currentCell.getValue().equals(previousCell.getValue()) ||
                        !currentCell.getOriginalValue().equals(previousCell.getOriginalValue());

                // בדיקה אם יש שינוי במאפייני ה-style
                boolean styleChanged = previousCell == null ||
                        (currentCell.getStyle() != null && previousCell.getStyle() != null &&
                                (!currentCell.getStyle().getAlignment().equals(previousCell.getStyle().getAlignment()) ||
                                        !currentCell.getStyle().getBackgroundColor().equals(previousCell.getStyle().getBackgroundColor()) ||
                                        !currentCell.getStyle().getTextColor().equals(previousCell.getStyle().getTextColor())));

                // אם יש שינוי בערך או ב-style, נגדיל את המונה
                if (valueChanged || styleChanged) {
                    changedCellsCount++;
                }
            }

            return changedCellsCount;
        } finally {
            sheetLock.unlock();
        }
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
        sheetLock.lock();  // נעילת תור
        try {
            Cell cell = getCell(coordinate);
            // בדיקה אם התא קיים
            if (cell == null) {
                return true; // התא ריק או לא קיים
            }
            return false;
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
    }

    public void addRange(String name, Range range) {
        sheetLock.lock();  // נעילת תור
        try {
            if (ranges.containsKey(name)) {
                throw new RuntimeException("Name already in use");
            }
            ranges.put(name, range);
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }

    }


    public Range getRange(String name) {
        return ranges.get(name);
    }

    public void removeRange(String name) {

        sheetLock.lock();  // נעילת תור
        try {
            if(!checkDeleteRange(name))
                throw new RuntimeException("Range in active use");

            ranges.remove(name);
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
    }


    public boolean checkDeleteRange(String name) {

        sheetLock.lock();  // נעילת תור
        try {
            String avgToFind="{AVERAGE,"+name+"}";
            String sumToFind="{SUM,"+name+"}";

            for(Cell cell : cellsInSheet.values()) {
                if(cell.getOriginalValue().contains(avgToFind)||cell.getOriginalValue().contains(sumToFind)) {
                    return false;
                }
            }
            return true;
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
    }



    // Get all cells in the range (returns a list of cells)
    public List<Cell> getCellsInRange(String name) {

        sheetLock.lock();  // נעילת תור
        try {
            Cell topLeft=getCellByString(ranges.get(name).getBoundaries().getFrom());
            Cell bottomRight= getCellByString(ranges.get(name).getBoundaries().getTo());

            List<Cell> cells = new ArrayList<>();
            for (int row = topLeft.getCoordinate().getRow(); row <= bottomRight.getCoordinate().getRow(); row++) {
                for (int col = topLeft.getCoordinate().getColumn(); col <= bottomRight.getCoordinate().getColumn(); col++) {
                    cells.add(this.getCell(row, col));
                }
            }
            return cells;
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
    }

    private Cell getCellByString(String s) {

        sheetLock.lock();  // נעילת תור
        try {

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
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }

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


    public List<Integer> getNumCellChangedHistory() {
        // מחזיר עותק של הרשימה כדי למנוע שינוי של הרשימה המקורית מחוץ למחלקה
        return new ArrayList<>(numCellChangedHistory);
    }


    public void initializaEmptyLists()
    {
        sheetLock.lock();  // נעילת תור
        try {
            versionHistory = new HashMap<>();
            numCellChangedHistory = new ArrayList<>();
            //ranges = new HashMap<>();
        } finally {
            sheetLock.unlock();  // שחרור הנעילה
        }
    }

}
