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
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class SheetControllerImpl implements SheetController {

    public ScrollPane sheetScrollPane;
    @FXML
    private GridPane sheetGridPane;

    private ObjectProperty<Label> selectedCell;

    private UImodel uiModel;

    private Coordinate selectedCoordinate; // משתנה שומר על הקואורדינטה הנבחרת


    SheetEngine sheetEngine = new SheetEngineImpl();





    @FXML
    private void initialize() {
        // יצירת מודל UI עבור התאים
        uiModel = new UImodel();

        // הגדרת המאזין לתא הנבחר
        selectedCell = new SimpleObjectProperty<>();
        selectedCell.addListener((observableValue, oldLabelSelection, newSelectedLabel) -> {
            if (oldLabelSelection != null) {
                oldLabelSelection.setId(null);
            }
            if (newSelectedLabel != null) {
                newSelectedLabel.setId("selected-cell");

                // קבלת הקואורדינטה של התא הנבחר והצבתה ב-selectedCoordinate
                selectedCoordinate = getCoordinateForLabel(newSelectedLabel);
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



    @Override
    public void alignCells(Pos alignment) {
        for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof Label)) {
            Label label = (Label) node; // המרה ל-Label
            label.setAlignment(alignment);
        }
    }


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
    public void updateSheet() {

        SheetDto sheetDto = sheetEngine.getCurrentSheetDTO();
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


    @Override
    public void updateCellContent(Coordinate coordinate, String content) {
        // עדכון ה-StringProperty במודל UI במקום ה-Label

        String Cell = coordinateToString(coordinate);
        sheetEngine.updateCellValue(Cell, content);
        // add exception!!!!!
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



    // פונקציה שמחזירה את הקואורדינטה לפי התווית שנבחרה
    private Coordinate getCoordinateForLabel(Label label) {
        Integer colIndex = GridPane.getColumnIndex(label);
        Integer rowIndex = GridPane.getRowIndex(label);

        if (colIndex != null && rowIndex != null) {
            return CoordinateCache.createCoordinate(rowIndex, colIndex);
        }
        return null;
    }

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
        return sheetEngine.getCellDTO(coordinateToString(getSelectedCoordinate())).getOriginalValue();
    }



    /*
    // פונקציה לעדכון תוכן תא
    @Override
    public void updateCellContent(Coordinate coordinate, String content) {
        uiModel.updateCell(coordinate, content); // עדכון התוכן במודל UI
    }

     */


}
