package controllerPack;

import dto.CellDto;
import dto.SheetDto;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
import java.util.List;

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

    private List<Integer> sortedRowOrder;

    // קואורדינטה של התא שממנו התחלנו את הבחירה
    private Coordinate startCoordinate;

    // קואורדינטה של התא בו עזבנו את הבחירה
    private Coordinate endCoordinate;



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

                // הוספת אירוע לחיצה לסימון תא
                label.setOnMouseClicked(event -> {
                    if (selectedCell.get() != null) {
                        selectedCell.get().setStyle("-fx-background-color: white;");
                    }
                    label.setStyle("-fx-background-color: #add8e6;");
                    selectedCell.set(label); // עדכון התא הנבחר
                });

                // הוספת אירוע מעבר עכבר
                label.setOnMouseEntered(event -> {
                    label.setStyle("-fx-background-color: lightblue;");
                });
                label.setOnMouseExited(event -> {
                    if (!label.equals(selectedCell.get())) {
                        label.setStyle("-fx-background-color: white;");
                    }
                });

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
                Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, col); // קואורדינטה לפי השורה המקורית
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

                /*
                // הוספת אירוע לחיצה לסימון תא
                label.setOnMouseClicked(event -> {
                    if (selectedCell.get() != null) {
                        selectedCell.get().setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    }
                    label.setStyle("-fx-background-color: #add8e6; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    selectedCell.set(label); // עדכון התא הנבחר
                });

                // הוספת אירוע מעבר עם העכבר
                label.setOnMouseEntered(event -> {
                    label.setStyle("-fx-background-color: lightblue; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                });

                label.setOnMouseExited(event -> {
                    if (!label.equals(selectedCell.get())) {
                        label.setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    }
                });

                 */

                // הוספת התווית לגריד, לפי השורה המודפסת (rowIndex) והעמודה הרגילה (col)
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
            System.out.println("Mouse pressed at: " + startCoordinate.getRow() + ", " + startCoordinate.getColumn());
        });

        // אירוע שמתחיל גרירה ברגע שהיא מזוהה
        label.setOnDragDetected(event -> {
            label.startFullDrag(); // מתחילים גרירה מלאה
            System.out.println("Drag detected, starting full drag.");
        });

        // אירוע מעבר מעל תא תוך כדי גרירה
        label.setOnMouseDragOver(event -> {
            endCoordinate = coordinate; // עדכון קואורדינטת סיום תוך כדי גרירה
            System.out.println("Mouse dragged over: " + endCoordinate.getRow() + ", " + endCoordinate.getColumn());
            highlightSelectedRange(startCoordinate, endCoordinate); // עידכון הסימון במהלך הגרירה
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
                    cell.setStyle("-fx-background-color: lightblue; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;"); // סימון התא בצבע כחול
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



    /*
    @Override
    public void updateSheet(SheetDto sheetDto) {


        // ניקוי ה-GridPane הקיים
        sheetGridPane.getChildren().clear();

        // קביעת גודל הגריד לפי מספר השורות והעמודות של הגיליון
        sheetGridPane.getRowConstraints().clear();
        sheetGridPane.getColumnConstraints().clear();

        final double cellWidth = 100.0; // רוחב קבוע לכל תא
        final double cellHeight = 30.0; // גובה קבוע לכל תא

        // הוספת כותרות עמודות
        for (int col = 0; col < sheetDto.getNumOfColumns(); col++) {
            char columnLetter = (char) ('A' + col); // A, B, C וכו'
            Label columnHeader = new Label(String.valueOf(columnLetter));
            columnHeader.setStyle("-fx-alignment: CENTER; -fx-padding: 5px;"); // יישור למרכז
            columnHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // מקסימום גודל להתאמה מלאה

            // החלת רקע ורוד
            sheetGridPane.add(columnHeader, col + 1, 0); // הכותרת בשורה 0, עמודה col + 1 (הכותרת מתחילה מעמודה 1)
            columnHeader.setStyle("-fx-background-color: lightpink; -fx-alignment: CENTER; -fx-padding: 5px;");
        }

        // הוספת כותרות שורות
        for (int row = 0; row < sheetDto.getNumOfRows(); row++) {
            Label rowHeader = new Label(String.valueOf(row + 1)); // 1, 2, 3 וכו'
            rowHeader.setStyle("-fx-alignment: CENTER; -fx-padding: 5px;"); // יישור למרכז
            rowHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // מקסימום גודל להתאמה מלאה

            // החלת רקע ורוד
            sheetGridPane.add(rowHeader, 0, row + 1); // הכותרת בעמודה 0, שורה row + 1 (הכותרת מתחילה משורה 1)
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

        // הוספת התאים מה-sheetDto
        for (int row = 1; row <= sheetDto.getNumOfRows(); row++) {
            for (int col = 1; col <= sheetDto.getNumOfColumns(); col++) {
                Coordinate coordinate = CoordinateCache.createCoordinate(row, col);
                CellDto cell = sheetDto.getCell(row, col);

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

                // הוספת אירוע לחיצה לסימון תא
                label.setOnMouseClicked(event -> {
                    if (selectedCell.get() != null) {
                        selectedCell.get().setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    }
                    label.setStyle("-fx-background-color: #add8e6; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    selectedCell.set(label); // עדכון התא הנבחר
                });

                // הוספת אירוע מעבר עם העכבר
                label.setOnMouseEntered(event -> {
                    label.setStyle("-fx-background-color: lightblue; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                });

                label.setOnMouseExited(event -> {
                    if (!label.equals(selectedCell.get())) {
                        label.setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    }
                });

                // הוספת התווית לגריד, הזזה של תאים ב-GridPane לפי השורות והעמודות (+1 עבור הכותרות)
                sheetGridPane.add(label, col , row );
            }
        }

        sheetGridPane.setGridLinesVisible(true); // הצגת קווי ההפרדה
    }

     */


    /*
    @Override
    public void updateCellContent(Coordinate coordinate, String content) {
        // עדכון ה-StringProperty במודל UI במקום ה-Label

        String Cell = coordinateToString(coordinate);
        sheetEngine.updateCellValue(Cell, content);
        // add exception!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        SheetDto sheetDto = sheetEngine.getCurrentSheetDTO();
        for (int row = 0; row < sheetDto.getNumOfRows(); row++) {
            for (int col = 0; col < sheetDto.getNumOfColumns(); col++) {
                Coordinate currcoordinate = CoordinateCache.createCoordinate(row, col);
                CellDto cell = sheetDto.getCell(row, col);
                // קישור ה-Label ל-StringProperty מתוך ה-UImodel
                String cellValue = (cell != null && cell.getValue() != null && !cell.getValue().isEmpty()) ? cell.getValue() : "";
                uiModel.updateCell(currcoordinate, cellValue);  // עדכון התא ב-UImodel
            }
        }

    }

     */



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


    public void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight) {
        // יצירת רשימה של עמודות (Character)
        List<Character> columns = new ArrayList<>();

        // לולאה שעוברת על העמודות מהעמודה השמאלית (topLeft.getColumn()) עד הימנית (bottomRight.getColumn())
        for (int col = topLeft.getColumn(); col <= bottomRight.getColumn(); col++) {
            // ממיר אינדקס עמודה לאות (למשל, 0 -> 'A', 1 -> 'B')
            char columnChar = (char) ('A' + col);
            columns.add(columnChar);
        }
        // קריאה לפונקציית המיון עם רשימת העמודות שנבנתה
        sortedRowOrder = sheetEngine.getCurrentSheetDTO().sortRowsByColumns(topLeft, bottomRight, columns);
    }


    public void resetSorting() {
        sortedRowOrder = sheetEngine.getCurrentSheetDTO().resetSoretedOrder();
    }

    //

    /*
    // פונקציה לעדכון תוכן תא
    @Override
    public void updateCellContent(Coordinate coordinate, String content) {
        uiModel.updateCell(coordinate, content); // עדכון התוכן במודל UI
    }

     */


}
