package controllerPack;

import dto.BoundariesDto;
import dto.CellDto;
import dto.RangeDto;
import dto.SheetDto;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import sheet.coordinate.impl.CoordinateImpl;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetControllerImpl implements SheetController {

    public ScrollPane sheetScrollPane;
    @FXML
    private GridPane sheetGridPane;

    @FXML
    private Label leftLabel;
    @FXML
    private Label rightLabel;

    private ObjectProperty<Label> selectedCell;

    private UImodel uiModel;

    private Coordinate selectedCoordinate; // משתנה שומר על הקואורדינטה הנבחרת

    private boolean readOnly = false;

    SheetEngine sheetEngine = new SheetEngineImpl();

    private List<Integer> sortedRowOrder; // printing list


    // רשימה שמורה של הסדר האחרון המלא לפני ביצוע סינון
    private List<Integer> lastSortedOrderBeforeFiltering;



    // קואורדינטה של התא שממנו התחלנו את הבחירה
    private Coordinate startCoordinate;

    // קואורדינטה של התא בו עזבנו את הבחירה
    private Coordinate endCoordinate;

    // הוספת Property עבור טווח נבחר
    private ObjectProperty<CellRange> selectedRange = new SimpleObjectProperty<>();

    // מפת מעקב אחרי מספר העמודות שבגללן כל שורה הוסרה
    private Map<Integer, Integer> removalCountMap = new HashMap<>();



    @FXML
    private void initialize() {
        // יצירת מודל UI עבור התאים
        uiModel = new UImodel();

        // הגדרת המאזין לתא הנבחר
        selectedCell = new SimpleObjectProperty<>();
        selectedCell.addListener((observableValue, oldLabelSelection, newSelectedLabel) -> {
            if (oldLabelSelection != null) {
                oldLabelSelection.setId(null); // ביטול בחירת תא קודם
            }
            if (newSelectedLabel != null) {
                newSelectedLabel.setId("selected-cell");

                // קבלת הקואורדינטה של התא הנבחר והצבתה ב-selectedCoordinate
                selectedCoordinate = getCoordinateForLabel(newSelectedLabel);  // עדכון הקואורדינטה עם השורה המקורית
            }
        });

        populateGrid(); // יצירת תאים והוספתם לגריד
    }


    // פונקציה המייצרת את הגריד ומכניסה תאים לתוך GridPane
    private void populateGrid() {
        // ניקוי ה-GridPane הקיים
        sheetGridPane.getChildren().clear();

        // הנחת תאים בגיליון (לדוגמה: 5x5 תאים)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                Coordinate coordinate = CoordinateCache.createCoordinate(row, col);
                Label label = new Label();

                // קישור StringProperty מה-UIModel לתצוגת ה-Label
                label.textProperty().bind(uiModel.getCellProperty(coordinate));


                sheetGridPane.add(label, col, row);
            }
        }
    }
    // פונקציה שמחזירה את הקואורדינטה המקורית עבור תווית נבחרת (label)
    private Coordinate getCoordinateForLabel(Label label) {
        // קבלת השורה והעמודה המוצגת ב-GridPane
        Integer displayedRow = GridPane.getRowIndex(label);
        Integer column = GridPane.getColumnIndex(label);

        // ודא שהשורה המוצגת גדולה מאפס לפני ההפחתה
        if (displayedRow > 0) {
            // תרגום השורה המוצגת לשורה המקורית לפי sortedRowOrder
            int originalRow = sortedRowOrder.get(displayedRow - 1);

            // החזרת הקואורדינטה המקורית
            return CoordinateCache.createCoordinate(originalRow, column);
        } else {
            // אם מדובר בשורה הראשונה (או שגיאה בהבאה של השורה), התייחס לשורה הראשונה
            return CoordinateCache.createCoordinate(sortedRowOrder.get(0), column);
        }
    }



    // פונקציה שמחזירה את הקואורדינטה המקורית עבור תווית נבחרת (label)
    private Coordinate getCoordinateForCell(Coordinate gridpaneCoordinate) {
        // קבלת השורה מה-GridPane
        int displayedRow = gridpaneCoordinate.getRow();
        int column = gridpaneCoordinate.getColumn();

        // תרגום השורה המוצגת לשורה המקורית על פי סדר המיון
        if (displayedRow > 0 && displayedRow <= sortedRowOrder.size()) {
            // קבלת השורה המקורית מה- sortedRowOrder
            int originalRow = sortedRowOrder.get(displayedRow - 1);

            // החזרת קואורדינטה עם השורה המקורית והעמודה ללא שינוי
            return CoordinateCache.createCoordinate(originalRow, column);
        } else {
            // במקרה של שגיאה או קואורדינטה מחוץ לטווח, נחזיר את השורה הראשונה
            return CoordinateCache.createCoordinate(sortedRowOrder.get(0), column);
        }
    }




    @Override
    public void alignCells(Pos alignment) {
        for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof Label)) {
            Label label = (Label) node; // המרה ל-Label
            label.setAlignment(alignment);
        }
    }
///

    // פונקציה לסימון תאים שתלויים אחד בשני
    @Override
    public void markCellsButtonActionListener(boolean isMarked) {
        if (isMarked) {
            // סימון תאים שתלויים
            for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof Label)) {
                Label label = (Label) node; // המרה ל-Label
                label.setStyle("-fx-background-color: yellow;");
            }
        } else {
            // החזרת הצבע המקורי
            for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof Label)) {
                Label label = (Label) node; // המרה ל-Label
                label.setStyle("-fx-background-color: white;");
            }
        }
    }

    @Override
    public void toggleCellColor(boolean isSelected) {
        if (isSelected) {
            for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof Label)) {
                Label label = (Label) node; // המרה ל-Label
                label.setStyle("-fx-background-color: red;");
            }
        } else {
            for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof Label)) {
                Label label = (Label) node; // המרה ל-Label
                label.setStyle("-fx-background-color: white;");
            }
        }
    }


    // שינוי רוחב עמודה שנייה
    @Override
    public void changeSecondColumnWidth(double width) {
        ColumnConstraints columnConstraints = sheetGridPane.getColumnConstraints().get(1);
        columnConstraints.setMinWidth(width);
        columnConstraints.setPrefWidth(width);
        columnConstraints.setMaxWidth(width);
    }

    // שינוי גובה שורה שנייה
    @Override
    public void changeSecondRowWidth(double width) {
        RowConstraints rowConstraints = sheetGridPane.getRowConstraints().get(1);
        rowConstraints.setMinHeight(width);
        rowConstraints.setPrefHeight(width);
        rowConstraints.setMaxHeight(width);
    }

    @Override
    public void updateSheet(SheetDto sheetDto) {
        // ניקוי ה-GridPane הקיים
        sheetGridPane.getChildren().clear();

        // קביעת גודל הגריד לפי מספר השורות והעמודות של הגיליון
        sheetGridPane.getRowConstraints().clear();
        sheetGridPane.getColumnConstraints().clear();

        final double cellWidth = 100.0; // רוחב קבוע לכל תא
        final double cellHeight = 30.0; // גובה קבוע לכל תא

        if (sortedRowOrder == null)
        {
            sortedRowOrder = sheetDto.resetSoretedOrder();
        }

        if (lastSortedOrderBeforeFiltering == null)
        {
            lastSortedOrderBeforeFiltering = new ArrayList<>();
            lastSortedOrderBeforeFiltering.addAll(sortedRowOrder);
        }

        // הוספת כותרות עמודות
        for (int col = 0; col < sheetDto.getNumOfColumns(); col++) {
            char columnLetter = (char) ('A' + col); // A, B, C וכו'
            Label columnHeader = new Label(String.valueOf(columnLetter));
            columnHeader.setStyle("-fx-alignment: CENTER; -fx-padding: 5px;");
            columnHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // החלת רקע ורוד
            sheetGridPane.add(columnHeader, col + 1, 0);
            columnHeader.setStyle("-fx-background-color: lightpink; -fx-alignment: CENTER; -fx-padding: 5px;");
        }

        // הוספת כותרות שורות לפי הסדר המודפס
        for (int rowIndex = 0; rowIndex < sortedRowOrder.size(); rowIndex++) {
            int actualRow = sortedRowOrder.get(rowIndex);  // שורה לפי הסדר המודפס
            Label rowHeader = new Label(String.valueOf(actualRow)); // הדפסה לפי מספר השורה המקורי
            rowHeader.setStyle("-fx-alignment: CENTER; -fx-padding: 5px;");
            rowHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // החלת רקע ורוד
            sheetGridPane.add(rowHeader, 0, rowIndex + 1); // מיקום השורה +1 כי שורה 0 לכותרות
            rowHeader.setStyle("-fx-background-color: lightpink; -fx-alignment: CENTER; -fx-padding: 5px;");
        }

        // קביעת גודל השורות והעמודות
        for (int row = 0; row < sheetDto.getNumOfRows(); row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(cellHeight); // גובה קבוע לשורות
            sheetGridPane.getRowConstraints().add(rowConstraints);
        }

        for (int col = 0; col < sheetDto.getNumOfColumns(); col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(cellWidth); // רוחב קבוע לעמודות
            sheetGridPane.getColumnConstraints().add(colConstraints);
        }

        // הוספת התאים לפי סדר השורות המודפסות
        for (int rowIndex = 0; rowIndex < sortedRowOrder.size(); rowIndex++) {
            int actualRow = sortedRowOrder.get(rowIndex);  // שורה לפי הסדר המודפס
            for (int col = 1; col <= sheetDto.getNumOfColumns(); col++) {
                Coordinate coordinate = CoordinateCache.createCoordinate(rowIndex + 1 , col); // coordinate according to gridpane
                CellDto cell = sheetDto.getCell(actualRow, col);

                // יצירת תווית חדשה
                Label label = new Label();

                // קישור ה-Label ל-StringProperty מתוך ה-UImodel
                String cellValue = (cell != null && cell.getValue() != null && !cell.getValue().isEmpty()) ? cell.getValue() : "";
                uiModel.updateCell(coordinate, cellValue);  // עדכון התא ב-UImodel
                label.textProperty().bind(uiModel.getCellProperty(coordinate)); // Binding

                // הגדרת רוחב מקסימלי כדי לחתוך טקסט אם הוא גדול מדי
                label.setMaxWidth(cellWidth);
                label.setWrapText(false); // ביטול גלישת טקסט
                label.setEllipsisString("..."); // הוספת שלוש נקודות במידת הצורך לחיתוך

                // הגדרת סגנון ברירת מחדל
                label.setStyle("-fx-alignment: CENTER_LEFT; -fx-background-color: white; -fx-padding: 5px;");



                // הוספת אירועים ללחיצת עכבר, גרירה ושחרור
                addMouseEvents(label, coordinate);


                sheetGridPane.add(label, col , rowIndex + 1); // הצגת התא בשורה החדשה
            }
        }

        sheetGridPane.setGridLinesVisible(true); // הצגת קווי ההפרדה
    }


    // הוספת אירועים ללחיצה, גרירה ושחרור עבור תאים
    private void addMouseEvents(Label label, Coordinate coordinate) {


        // אירוע לחיצה על עכבר (התחלה)
        label.setOnMousePressed(event -> {
            startCoordinate = coordinate; // שמירת קואורדינטת התחלה
            endCoordinate = coordinate;
            setSelectedRange(startCoordinate, endCoordinate);
            selectedCell.set((Label)getNodeByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
            highlightSelectedRange(startCoordinate, endCoordinate);
            System.out.println("Mouse pressed at: " + startCoordinate.getRow() + ", " + startCoordinate.getColumn());
        });

        // אירוע שמתחיל גרירה ברגע שהיא מזוהה
        label.setOnDragDetected(event -> {
            label.startFullDrag(); // מתחילים גרירה מלאה
            System.out.println("Drag detected, starting full drag.");
        });

        // אירוע מעבר מעל תא תוך כדי גרירה
        label.setOnMouseDragOver(event -> {
            if(coordinate != endCoordinate)
            {
                System.out.println("Mouse dragged over: " + endCoordinate.getRow() + ", " + endCoordinate.getColumn());
                endCoordinate = coordinate;
                highlightSelectedRange(startCoordinate, endCoordinate); // עידכון הסימון במהלך הגרירה
                if (startCoordinate != endCoordinate)
                {
                    selectedCell.set(null);
                    setSelectedRange(startCoordinate, endCoordinate);
                }
                else
                {
                    selectedCell.set((Label)getNodeByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
                }
            }
        });

        // אירוע שחרור עכבר
        label.setOnMouseReleased(event -> {
            //endCoordinate = coordinate; // עדכון קואורדינטת סיום תוך כדי גרירה
            System.out.println("Mouse released at: " + endCoordinate.getRow() + ", " + endCoordinate.getColumn());
            highlightSelectedRange(startCoordinate, endCoordinate); // סימון הטווח שנבחר
            if (startCoordinate == endCoordinate) {
                selectedCell.set((Label)getNodeByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
            }
            else{
                selectedCell.set(null);
                selectedCoordinate = null;
                setSelectedRange(startCoordinate, endCoordinate);
            }
        });
    }






    // פונקציה לסימון טווח תאים לפי הגרירה (עבודה על Label ולא Node)
    private void highlightSelectedRange(Coordinate start, Coordinate end) {
        // ניקוי כל התאים מסימון קודם, למעט הכותרות (שורות ועמודות)
        sheetGridPane.getChildren().forEach(node -> {
            if (node instanceof Label) { // בדיקה אם ה-Node הוא אובייקט Label
                Label label = (Label) node;

                // בדיקת מיקום התא - אם הוא בכותרת עמודה (שורה 0) או בכותרת שורה (עמודה 0)
                Integer row = GridPane.getRowIndex(node);
                Integer col = GridPane.getColumnIndex(node);

                // אם התא הוא לא כותרת עמודה או שורה, נצבע אותו בלבן
                if (row != null && col != null && row > 0 && col > 0) {
                    label.setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                }
            }
        });

        // סימון טווח חדש
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startCol = Math.min(start.getColumn(), end.getColumn());
        int endCol = Math.max(start.getColumn(), end.getColumn());

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Label cell = (Label) getNodeByCoordinate(row, col);
                if (cell != null) {
                    cell.setStyle("-fx-background-color: #ffd5e9; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;"); // סימון התא בצבע כחול
                }
            }
        }
    }


    // פונקציה שמחזירה את ה-Node (Label) לפי קואורדינטות
    private Label getNodeByCoordinate(int row, int col) {
        for (Node node : sheetGridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                return (Label) node; // החזרה של ה-Label במקום Node
            }
        }
        return null;
    }






    @Override
    public void updateCellContent(Coordinate coordinate, String content) {
        try {
            // המרה של הקואורדינטה למחרוזת (לדוגמה: "A1")
            String cellReference = coordinateToString(coordinate);

            // עדכון הערך ב-sheetEngine
            sheetEngine.updateCellValue(cellReference, content);

            // קבלת ה- SheetDto המעודכן
            SheetDto sheetDto = sheetEngine.getCurrentSheetDTO();

            // עדכון כל התאים ב-UI Model
            for (int row = 0; row < sheetDto.getNumOfRows(); row++) {
                for (int col = 0; col < sheetDto.getNumOfColumns(); col++) {
                    Coordinate currCoordinate = CoordinateCache.createCoordinate(row, col);
                    CellDto cell = sheetDto.getCell(row, col);

                    // קישור ה-Label ל-StringProperty מתוך ה-UImodel
                    String cellValue = (cell != null && cell.getValue() != null && !cell.getValue().isEmpty()) ? cell.getValue() : "";
                    uiModel.updateCell(currCoordinate, cellValue);  // עדכון כל התאים ב-UImodel
                }
            }

        } catch (Exception e) {
            // טיפול בשגיאות
            System.err.println("Error updating cell content: " + e.getMessage());
            e.printStackTrace();
        }
    }





    // פונקציה שמחזירה תווית (Label) לפי קואורדינטה
    public Label getLabelByCoordinate(Coordinate coordinate) {
        for (Node node : sheetGridPane.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(node);
            Integer rowIndex = GridPane.getRowIndex(node);

            if (colIndex != null && rowIndex != null && coordinate.getColumn() == colIndex && coordinate.getRow() == rowIndex) {
                if (node instanceof Label) {
                    return (Label) node;
                }
            }
        }
        return null; // אם לא נמצא התא המתאים
    }

    public Coordinate getSelectedCoordinate() {
        return selectedCoordinate;  // החזרת הקואורדינטה של התא הנבחר
    }

    public ObjectProperty<Label> selectedCellProperty() {
        return selectedCell;  // החזרת ה-Property של התא הנבחר
    }



    /*
    // פונקציה שמחזירה את הקואורדינטה לפי התווית שנבחרה
    private Coordinate getCoordinateForLabel(Label label) {
        Integer colIndex = GridPane.getColumnIndex(label);
        Integer rowIndex = GridPane.getRowIndex(label);

        if (colIndex != null && rowIndex != null) {
            return CoordinateCache.createCoordinate(rowIndex, colIndex);
        }
        return null;
    }

     */

    @Override
    public void loadSheetFromFile(String filename) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException {
        sheetEngine.loadSheetFromXML(filename);
    }



    private String coordinateToString(Coordinate coordinate) {
        // המרת העמודה לאות (A, B, C, ...)
        int column = coordinate.getColumn();
        char columnLetter = (char) ('A' + column - 1); // המרה מ-1 ל-A, מ-2 ל-B וכדומה

        // המרת השורה למספר (ללא שינוי)
        int row = coordinate.getRow();

        // חיבור האות למספר ויצירת המחרוזת הסופית
        return String.valueOf(columnLetter) + row;
    }


    public String getSelectedCoordinateOriginalValue()
    {
        CellDto cell = sheetEngine.getCellDTO(coordinateToString(getSelectedCoordinate()));
        if(cell != null)
        {
            return cell.getOriginalValue();
        }
        return ""; // this will
    }


    public List<Integer> getVersionList()
    {
        return sheetEngine.getNumChangedCellsInAllVersions();
    }


    public void loadSheetVersion(int version)
    {
        SheetDto versionDTO = sheetEngine.getVersionDto(version);
        updateSheet(versionDTO);
    }


    public void loadSheetCurrent()
    {
        SheetDto sheetDtoCurrent = sheetEngine.getCurrentSheetDTO();
        updateSheet(sheetDtoCurrent);
    }


    public SheetDto getVersionDto(int version)
    {
        return sheetEngine.getVersionDto(version);
    }


    public void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight, List<Character> colList) {
        // המרת הטווח לרשימת שורות על פי הסדר הקיים ב-sortedRowOrder
        List<Integer> rowsInRange = getRowsFromCoordinates(topLeft, bottomRight);


        List<Integer> rangeSorted = sheetEngine.getCurrentSheetDTO().sortRowsByColumns(rowsInRange, colList);

        // החזרת השורות הממוינות לרשימת sortedRowOrder במקום הנכון
        replaceSortedRangeInOrder(rowsInRange, rangeSorted);



        // עדכון lastSortedOrder עם הסדר החדש
        lastSortedOrderBeforeFiltering = new ArrayList<>(sortedRowOrder);
    }



    // הפונקציה שמחזירה את השורות הממוינות לטווח במקום הנכון ב-sortedRowOrder
    private void replaceSortedRangeInOrder(List<Integer> originalRange, List<Integer> sortedRange) {
        int startIndex = -1;

        // מציאת האינדקס ההתחלתי של הטווח ב-sortedRowOrder
        for (int i = 0; i < sortedRowOrder.size(); i++) {
            if (sortedRowOrder.get(i).equals(originalRange.get(0))) {
                startIndex = i;
                break;
            }
        }

        // וידוא שמצאנו את המקום הנכון
        if (startIndex != -1) {
            // החלפת הטווח הממויין ב-sortedRowOrder
            for (int i = 0; i < sortedRange.size(); i++) {
                sortedRowOrder.set(startIndex + i, sortedRange.get(i));
            }
        }
    }




    private List<Integer> getRowsFromCoordinates(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rowsInRange = new ArrayList<>();

        // מעבר על כל שורה בטווח שנבחר
        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            // השגת השורה המתאימה לפי אינדקס ב-sortedRowOrder
            if (gridRow - 1 < sortedRowOrder.size()) {
                int actualRow = sortedRowOrder.get(gridRow - 1); // הוצאה של השורה המקורית מתוך sortedRowOrder
                rowsInRange.add(actualRow);
            }
        }

        return rowsInRange;
    }




    public void resetSorting() {
        sortedRowOrder = sheetEngine.getCurrentSheetDTO().resetSoretedOrder();
    }



    // Getter עבור selectedRangeProperty
    public ObjectProperty<CellRange> selectedRangeProperty() {
        return selectedRange;
    }

    // פונקציה להגדיר את הטווח הנבחר
    private void setSelectedRange(Coordinate topLeft, Coordinate bottomRight) {
        selectedRange.set(new CellRange(topLeft, bottomRight));  // יוצרים טווח חדש ומעדכנים את ה-Property
    }
    //

    /*
    // פונקציה לעדכון תוכן תא
    @Override
    public void updateCellContent(Coordinate coordinate, String content) {
        uiModel.updateCell(coordinate, content); // עדכון התוכן במודל UI
    }

     */

    public Map<String, RangeDto> getRanges()
    {
        return sheetEngine.getCurrentSheetDTO().getRanges();
    }


    //need to update with correct highlight
    public void highlightFunctionRange(String rangeName)
    {
        BoundariesDto currBoundaries = sheetEngine.getCurrentSheetDTO().getRanges().get(rangeName).getBoundaries();
        Coordinate from = CoordinateCache.createCoordinateFromString(currBoundaries.getFrom());
        Coordinate to = CoordinateCache.createCoordinateFromString(currBoundaries.getTo());

        highlightSelectedRange(from, to); ////// need to update to work with after sorting where cells are scattered!
    }


    //need to update with correct highlight
    public boolean deleteRange(String rangeName)
    {
        sheetEngine.deleteRange(rangeName);
        return true;
    }



    public List<String> getSelectedColumns() {
        // בדיקה אם startCoordinate ו-endCoordinate מאותחלים
        if (startCoordinate == null || endCoordinate == null) {
            return new ArrayList<>(); // החזר רשימה ריקה אם לא אותחלו
        }

        // השגת ערכי העמודה ההתחלתית והסופית
        int startColumn = startCoordinate.getColumn();
        int endColumn = endCoordinate.getColumn();

        // הבטחת הסדר הנכון (אם נבחר מימין לשמאל)
        if (startColumn > endColumn) {
            int temp = startColumn;
            startColumn = endColumn;
            endColumn = temp;
        }

        // יצירת רשימה להחזקת שמות העמודות
        List<String> selectedColumns = new ArrayList<>();

        // מעבר על העמודות שבטווח והמרתן לשמות
        for (int col = startColumn; col <= endColumn; col++) {
            selectedColumns.add(convertColumnNumberToString(col));
        }

        return selectedColumns;
    }

    // פונקציה המסייעת להמרת מספר עמודה למחרוזת (למשל: 1 -> "A", 2 -> "B")
    private String convertColumnNumberToString(int columnNumber) {
        StringBuilder columnName = new StringBuilder();
        while (columnNumber > 0) {
            int remainder = (columnNumber - 1) % 26;
            columnName.insert(0, (char)(remainder + 'A'));
            columnNumber = (columnNumber - 1) / 26;
        }
        return columnName.toString();
    }


    public Map<String, List<String>> getUniqueValuesInRange(Coordinate topLeft, Coordinate bottomRight)
    {

        List<Integer> rows = getRowsFromCoordinates(topLeft, bottomRight);


        return sheetEngine.getUniqueValuesInRange(rows,getSelectedColumns());
    }



    public void removeRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight) {
        // חיפוש כל השורות שיש להסיר על פי הערך המסומן והעמודה
        List<Integer> rowsToRemove = new ArrayList<>();

        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            // do this more readble!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            int actualRow = lastSortedOrderBeforeFiltering.get(gridRow - 1); // מקבל את השורה המקורית לפי התרגום ב-sortedRowOrder
            Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, convertColumnNameToNumber(columnName));
            CellDto cell = sheetEngine.getCellDTO(coordinateToString(coordinate));

            if (cell != null && cell.getValue().equals(value)) {
                // עדכון מפת הספירה
                removalCountMap.put(actualRow, removalCountMap.getOrDefault(actualRow, 0) + 1);
                rowsToRemove.add(actualRow);
            }
        }

        // הסרת השורות מרשימת sortedRowOrder
        sortedRowOrder.removeAll(rowsToRemove);

        // עדכון התצוגה שלך, לוודא שהרשימה מעודכנת כראוי ב-UI
        updateSheet(sheetEngine.getCurrentSheetDTO());
    }

    public void addRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight) {
        // השגת רשימת השורות המתורגמות לפי הסדר הנוכחי
        List<Integer> rowsInRange = getRowsFromCoordinatesBeforeFiltering(topLeft, bottomRight);

        // חיפוש כל השורות שיש להחזיר לפי הערך המסומן מחדש
        List<Integer> rowsToAdd = new ArrayList<>();

        // מעבר על השורות המתורגמות שהתקבלו
        for (Integer actualRow : rowsInRange) {
            Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, convertColumnNameToNumber(columnName));
            CellDto cell = sheetEngine.getCellDTO(coordinateToString(coordinate));

            if (cell != null && cell.getValue().equals(value)) {
                // עדכון מפת הספירה והחזרה לשורות ההדפסה
                int count = removalCountMap.getOrDefault(actualRow, 0);
                if (count > 0) {
                    removalCountMap.put(actualRow, count - 1);
                }

                // אם הספירה מגיעה ל-0, ניתן להחזיר את השורה
                if (removalCountMap.get(actualRow) == 0) {
                    removalCountMap.remove(actualRow); // הסרת הרישום מהמפה
                    rowsToAdd.add(actualRow);
                }
            }
        }

        // הוספת כל שורה במיקום הנכון ב-sortedRowOrder לפי הסדר המקורי
        for (Integer row : rowsToAdd) {
            insertRowInCorrectOrder(row);
        }

        // עדכון התצוגה שלך, לוודא שהרשימה מעודכנת כראוי ב-UI
        updateSheet(sheetEngine.getCurrentSheetDTO());
    }



    // פונקציה המסייעת להוספת השורה במיקום הנכון לפי הסדר האחרון ששמור ב-lastSortedOrder
    private void insertRowInCorrectOrder(Integer row) {
        if (lastSortedOrderBeforeFiltering == null) {
            // במקרה ואין סדר מיון קודם, פשוט נוסיף לפי סדר כרונולוגי
            for (int i = 0; i < sortedRowOrder.size(); i++) {
                if (sortedRowOrder.get(i) > row) {
                    sortedRowOrder.add(i, row);
                    return;
                }
            }
            sortedRowOrder.add(row); // אם לא נמצא מקום מתאים, נוסיף לסוף
            return;
        }

        // במקרה שיש סדר מיון שמור ב-lastSortedOrder, נמצא את המקום הנכון להוסיף את השורה
        int indexInLastSorted = lastSortedOrderBeforeFiltering.indexOf(row);
        for (int i = 0; i < sortedRowOrder.size(); i++) {
            int currentRowInOrder = sortedRowOrder.get(i);
            if (lastSortedOrderBeforeFiltering.indexOf(currentRowInOrder) > indexInLastSorted) {
                sortedRowOrder.add(i, row);
                return;
            }
        }

        // אם לא נמצא מקום מתאים, נוסיף לסוף הרשימה
        sortedRowOrder.add(row);
    }



    public int translateRow(int uiRowIndex) {
        // המרת השורה מה-UI למספר שורה ברשימת sortedRowOrder
        if (uiRowIndex >= 0 && uiRowIndex < sortedRowOrder.size()) {
            return sortedRowOrder.get(uiRowIndex);
        }
        throw new IllegalArgumentException("Row index out of bounds: " + uiRowIndex);
    }


    private int convertColumnNameToNumber(String columnName) {
        int columnNumber = 0;

        for (int i = 0; i < columnName.length(); i++) {
            char currentChar = columnName.charAt(i);

            // חישוב מספר העמודה בהתבסס על הערך של האותיות (לדוגמה 'A' = 1, 'B' = 2 וכו')
            columnNumber = columnNumber * 26 + (currentChar - 'A' + 1);
        }

        return columnNumber;
    }




    private List<Integer> getRowsFromCoordinatesBeforeFiltering(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rowsInRange = new ArrayList<>();

        // מעבר על כל שורה בטווח שנבחר
        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            // השגת השורה המתאימה לפי אינדקס ב-sortedRowOrder
            if (gridRow - 1 < lastSortedOrderBeforeFiltering.size()) {
                int actualRow = lastSortedOrderBeforeFiltering.get(gridRow - 1); // הוצאה של השורה המקורית מתוך sortedRowOrder
                rowsInRange.add(actualRow);
            }
        }

        return rowsInRange;
    }




}
