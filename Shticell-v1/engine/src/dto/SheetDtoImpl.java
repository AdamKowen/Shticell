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
            Coordinate coordinate = entry.getKey(); // מפתח (קואורדינטות התא)
            Cell cell = entry.getValue(); // ערך (אובייקט תא)

            // יצירת CellDto מהאובייקט Cell
            CellDto cellDto = new CellDtoImpl(cell);

            // הכנסת ה-CellDto למפה החדשה
            currSheetToDto.put(coordinate, cellDto);
        }

        this.cellsInSheet = currSheetToDto;



        Map<String, Range> currRanges = sheet.getRanges();
        Map<String, RangeDto> currRangesToDto = new HashMap<>();

        for (Map.Entry<String, Range> entry : currRanges.entrySet()) {
            String name = entry.getKey(); // מפתח (name)
            Range range = entry.getValue(); // ערך (range)

            // יצירת rangedto מהאובייקט range
            RangeDto rangeDto = new RangeDto(range);

            // הכנסת ה-range למפה החדשה
            currRangesToDto.put(name, rangeDto);
        }

        this.ranges = currRangesToDto;

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



    // פונקציה למיון השורות בטווח מסוים על פי עמודות נבחרות
    public List<Integer> sortRowsByColumns(Coordinate topLeft, Coordinate bottomRight, List<Character> columnChars) {
        // ממירים את ה-characters של העמודות למספרי אינדקסים
        List<Integer> columnIndices = convertColumnsToIndices(columnChars);

        // שליפת השורות הרלוונטיות מהמפה לפי הטווח
        Map<Integer, List<CellDto>> rowsInRange = getRowsInRange(topLeft, bottomRight);

        // מיון השורות בטווח שנבחר בלבד
        List<Integer> sortedRowNumbers = sortRowsByGivenColumns(rowsInRange, columnIndices);

        return sortedRowNumbers;
    }

    // ממיר רשימת עמודות מסוג char למספרי עמודות
    private List<Integer> convertColumnsToIndices(List<Character> columnChars) {
        return columnChars.stream()
                .map(c -> (int) c - 'A') // המרת כל עמודה לאינדקס מספרי (נניח ש-A מתחיל מ-0)
                .collect(Collectors.toList());
    }

    // שליפת השורות בטווח שנבחר
    private Map<Integer, List<CellDto>> getRowsInRange(Coordinate topLeft, Coordinate bottomRight) {
        Map<Integer, List<CellDto>> rows = new HashMap<>();

        for (int row = topLeft.getRow(); row <= bottomRight.getRow(); row++) {
            List<CellDto> cellsInRow = new ArrayList<>();
            for (int col = topLeft.getColumn(); col <= bottomRight.getColumn(); col++) {
                Coordinate coord = CoordinateCache.createCoordinate(row,col);
                if (cellsInSheet.containsKey(coord)) {
                    cellsInRow.add(cellsInSheet.get(coord));
                }
            }
            rows.put(row, cellsInRow);
        }
        return rows;
    }


    private List<Integer> sortRowsByGivenColumns(Map<Integer, List<CellDto>> rowsInRange, List<Integer> columnIndices) {


        /*
        // קבלת כל מספרי השורות בסדר המקורי
        List<Integer> allRows = cellsInSheet.keySet().stream()
                .map(Coordinate::getRow)
                .distinct()  // במקרה של שורות כפולות
                .sorted()    // שמירה על סדר השורות המקורי
                .collect(Collectors.toList());

         */

        // יצירת רשימה של כל השורות (1 עד numOfRows)
        List<Integer> allRows = new ArrayList<>();
        for (int i = 1; i <= numOfRows; i++) {
            allRows.add(i);  // מוסיף את כל השורות לפי הסדר המקורי
        }

        // מיון השורות שנמצאות בטווח שנבחר
        List<Integer> sortedRowNumbers = rowsInRange.keySet().stream()
                .sorted((row1, row2) -> compareRowsByColumns(row1, row2, rowsInRange, columnIndices))
                .collect(Collectors.toList());


        /*
        // יצירת רשימה חדשה של כל השורות, שבה נשמור את השורות הממוינות במקום הנכון
        List<Integer> sortedAllRows = new ArrayList<>(allRows);

        // מחליפים את השורות הממוינות בתוך רשימת כל השורות
        int indexInSorted = 0;
        for (int i = 0; i < allRows.size(); i++) {
            int currentRow = allRows.get(i);

            // אם השורה הנוכחית נמצאת בטווח המיון, מחליפים אותה בשורה הממוינת
            if (rowsInRange.containsKey(currentRow)) {
                sortedAllRows.set(i, sortedRowNumbers.get(indexInSorted));
                indexInSorted++;
            }
        }

         */



        // החלפת השורות הממוינות בתוך רשימת כל השורות
        int sortedIndex = 0;
        for (int i = 0; i < allRows.size(); i++) {
            int currentRow = allRows.get(i);

            // אם השורה הנוכחית נמצאת בטווח המיון, מחליפים אותה בשורה הממוינת
            if (rowsInRange.containsKey(currentRow)) {
                allRows.set(i, sortedRowNumbers.get(sortedIndex));
                sortedIndex++;
            }
        }


        // מחזירים את רשימת כל השורות עם השורות שבטווח ממוינות והשאר בסדר המקורי שלהן
        return allRows;
    }


    // פונקציה שמבצעת השוואה בין שתי שורות לפי עמודות נבחרות
    private int compareRowsByColumns(int row1, int row2, Map<Integer, List<CellDto>> rowsInRange, List<Integer> columnIndices) {
        // עבור כל עמודה שבחרנו, בודקים את הערכים שלה עבור כל שורה
        for (Integer columnIndex : columnIndices) {
            CellDto cell1 = getCellInRow(row1, columnIndex, rowsInRange);
            CellDto cell2 = getCellInRow(row2, columnIndex, rowsInRange);

            // מבצעים השוואה בין הערכים המספריים (אם הם קיימים)
            int comparison = compareCells(cell1, cell2);  // השוואה בכיוון הנכון (cell1 קודם)
            if (comparison != 0) {
                return comparison; // אם יש תוצאה חיובית, מחזירים אותה
            }
        }
        return 0; // אם כל הערכים זהים, לא משנים את הסדר
    }


    // שליפת תא מסוים משורה לפי אינדקס העמודה
    private CellDto getCellInRow(int row, int column, Map<Integer, List<CellDto>> rowsInRange) {
        List<CellDto> rowCells = rowsInRange.get(row);

        // חיפוש התא לפי אינדקס העמודה
        for (CellDto cell : rowCells) {
            if (cell.getCoordinate().getColumn() == column) {
                return cell;
            }
        }

        // אם לא מצאנו תא, מחזירים תא ריק (יכול להיות בהתאם לאיך שאתה מייצג תאים ריקים)
        return null;
    }


    // השוואה בין שני תאים על בסיס ערכים מספריים
    private int compareCells(CellDto cell1, CellDto cell2) {
        if (cell1 == null && cell2 == null) {
            return 0; // שני התאים ריקים, אין שינוי
        } else if (cell1 == null) {
            return -1; // תא ריק נחשב כקטן
        } else if (cell2 == null) {
            return 1;  // תא ריק נחשב כקטן
        }

        // מיון לפי ערכים מספריים, נתעלם ממחרוזות ומערכים לא מספריים
        try {
            double value1 = Double.parseDouble(cell1.getValue());
            double value2 = Double.parseDouble(cell2.getValue());
            return Double.compare(value1, value2);
        } catch (NumberFormatException e) {
            return 0; // אם לא ניתן להמיר למספר, לא משנים את הסדר
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

}
