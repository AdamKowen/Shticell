package controllerPack;
import dto.CellDto;
import dto.SheetDto;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import javafx.scene.input.DragEvent;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {

    private Label selectedLabel = null;
    private Coordinate selectedCoordinate = null;




    @FXML
    private Button LoadButton;

    @FXML
    private Label fileNameLabel;

    @FXML
    private SheetController sheetComponentController;

    @FXML
    private SheetController sheetVersionController;


    @FXML
    private ComboBox<Object> versionComboBox;

    //@FXML
    //private ScrollPane sheetScrollPane;

    @FXML
    private TextField cellInputContentTextField;


    @FXML
    private TextField topLeftBox;

    @FXML
    private TextField bottomRightBox;


    @FXML
    private Button sort;

    @FXML
    private Button resetsort;


    @FXML
    private Label selectedCellLabel;

    @FXML
    private ListView<String> colList; // ListView לרשימת העמודות

    private String draggedItem;  // נשמור את האיבר הנגרר


    @FXML
    private ListView<String> listOfRanges; // אותו שם שהשתמשת ב-Scene Builder


    @FXML
    private void initialize() {
        // Listener לשינויי הבחירה של התא
        sheetComponentController.selectedCellProperty().addListener((observable, oldLabel, newLabel) -> {
            if (newLabel != null) {
                // עדכון תיבת הטקסט עם הערך של התא הנבחר
                cellInputContentTextField.setText(sheetComponentController.getSelectedCoordinateOriginalValue());

                // הצגת הקואורדינטות של התא הנבחר
                selectedCoordinate = sheetComponentController.getSelectedCoordinate();
                selectedCellLabel.setText("Selected cell: " + selectedCoordinate);

                // הגדרת המיקוד על תיבת הטקסט כך שהסמן יהיה בפנים
                cellInputContentTextField.requestFocus();

                // ממקם את הסמן בסוף הטקסט הקיים
                cellInputContentTextField.positionCaret(cellInputContentTextField.getText().length());
            } else {
                cellInputContentTextField.setText("");
                selectedCellLabel.setText("Selected cell: none");
            }
        });



        // הוספת Listener לטווח תאים
        sheetComponentController.selectedRangeProperty().addListener((observable, oldRange, newRange) -> {
            if (newRange != null) {
                Coordinate topLeft = newRange.getTopLeft();
                Coordinate bottomRight = newRange.getBottomRight();
                selectedCellLabel.setText("Selected range: " + topLeft + " to " + bottomRight);
                topLeftBox.setText(topLeft.toString());
                bottomRightBox.setText(bottomRight.toString());
            } else {
                selectedCellLabel.setText("No range selected");
            }
        });




        // הוספת Listener ללחיצה על Enter בתיבת הטקסט
        cellInputContentTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // עדכון התא שנבחר בתוכן החדש
                Coordinate selectedCoordinate = sheetComponentController.getSelectedCoordinate();
                if (selectedCoordinate != null) {
                    String newValue = cellInputContentTextField.getText();
                    sheetComponentController.updateCellContent(selectedCoordinate, newValue);

                    versionComboBox.getItems().clear();
                    // קבלת רשימת הגרסאות
                    List<Integer> versions = sheetComponentController.getVersionList();
                    // עבור על הרשימה ובנה מחרוזות עם מספרי גרסאות ומספרי שינויים
                    for (int i = 0; i < versions.size(); i++) {
                        int versionNumber = i + 1; // מספר הגרסה מתחיל מ-1
                        int changes = versions.get(i); // מספר השינויים לאותה גרסה
                        String versionText = "Version " + versionNumber + " - " + changes + " changes";

                        // הוסף את המחרוזת ל-ComboBox
                        versionComboBox.getItems().add(versionText);
                    }
                }
            }
        });


        // Listener ללחיצה על כפתור ה-sort
        sort.setOnAction(event -> {
            // מקבל את הקואורדינטות מתיבות הטקסט
            Coordinate topLeft = CoordinateCache.createCoordinateFromString(topLeftBox.getText());
            Coordinate bottomRight = CoordinateCache.createCoordinateFromString(bottomRightBox.getText());

            if (topLeft != null && bottomRight != null) {
                // קריאה לפונקציית המיון שלך במנוע הגיליון
                sheetComponentController.sortRowsInRange(topLeft, bottomRight);

                // רענון התצוגה כדי להציג את הלוח אחרי המיון
                refreshSheetDisplay();
            } else {
                System.out.println("Invalid coordinates entered.");
            }
        });

        // Listener ללחיצה על כפתור ה-reset (ביטול המיון)
        resetsort.setOnAction(event -> {
            sheetComponentController.resetSorting();
            refreshSheetDisplay();
        });



        // הוסף מאזין לבחירה ב-ComboBox
        versionComboBox.setOnAction(event -> {
            // קבל את האינדקס של הבחירה (האינדקס תואם לסדר של הגרסאות)
            int selectedIndex = versionComboBox.getSelectionModel().getSelectedIndex();

            if (selectedIndex != -1) { // ודא שמשהו נבחר
                // טען את הגרסה שנבחרה לפי האינדקס
                sheetVersionController.updateSheet(sheetComponentController.getVersionDto(selectedIndex+1)); // לדוגמה, טען את הגרסה שנבחרה
            }
        });


        addDragEvents(); // לרשימת מיון






        listOfRanges.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetComponentController.highlightFunctionRange(newValue); // קריאה לפונקציה שמדגישה את הטווח
            }
        });


    }

    private void addDragEvents() {

        ObservableList<String> items = FXCollections.observableArrayList("A", "B", "C", "D", "E");  // דוגמה לרשימת עמודות
        colList.setItems(items);
        colList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem());
                    db.setContent(content);

                    // יצירת DragView עם האות הנגררת
                    WritableImage snapshot = cell.snapshot(null, null);  // יצירת snapshot של התא הנגרר
                    db.setDragView(snapshot);  // הצגת הטקסט במקום אייקון ברירת המחדל

                    event.consume();
                }
            });


            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });

            cell.setOnDragEntered(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    cell.setStyle("-fx-background-color: lightblue;");
                }
            });

            cell.setOnDragExited(event -> {
                cell.setStyle("");
            });


            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && cell.getItem() != null) {
                    int draggedIdx = items.indexOf(db.getString());
                    int thisIdx = items.indexOf(cell.getItem());

                    String temp = items.get(draggedIdx);
                    items.set(draggedIdx, items.get(thisIdx));
                    items.set(thisIdx, temp);

                    List<String> copy = new ArrayList<>(items);
                    items.clear();
                    items.addAll(copy);

                    event.setDropCompleted(true);
                    colList.getSelectionModel().select(thisIdx);
                    event.consume();
                }
            });


            cell.setOnDragDone(event -> {
                event.consume();
            });

            return cell;
        });
    }






    // פונקציה לטעינת העמודות לתוך הרשימה
    private void initializeColumnList(List<String> columns) {
        colList.getItems().clear(); // ניקוי הרשימה אם יש פריטים קודמים
        colList.getItems().addAll(columns); // הוספת שמות העמודות
    }


    private void enableColumnReordering() {
        colList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();

            cell.textProperty().bind(cell.itemProperty()); // קישור הטקסט של התאים לנתונים ברשימה

            // הגדרת גרירה
            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) return;

                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(cell.getItem());
                db.setContent(cc);
                event.consume();
            });

            // קבלת הפריט שנגרר
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // שחרור והכנסת העמודה במקום הנכון
            cell.setOnDragDropped(event -> {
                if (cell.getItem() == null) return;

                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int draggedIndex = colList.getItems().indexOf(db.getString());
                    int thisIndex = colList.getItems().indexOf(cell.getItem());

                    // הזזת העמודה לרשימה במקום הנכון
                    String draggedItem = colList.getItems().remove(draggedIndex);
                    colList.getItems().add(thisIndex, draggedItem);

                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            // מחיקת גרירה לאחר סיום
            cell.setOnDragDone(DragEvent::consume);

            return cell;
        });
    }




    // פונקציה לרענון התצוגה של הגיליון אחרי המיון
    private void refreshSheetDisplay() {
        sheetComponentController.loadSheetCurrent();
    }


    @FXML
    void loadButtonActionListener(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");

        // הוספת סינון לקבצים אם אתה רוצה לאפשר בחירת סוגים מסוימים בלבד (אופציונלי)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // מציג את שם הקובץ על הלייבל
            try {

                sheetComponentController.loadSheetFromFile(file.getPath());
                //sheetEngine.loadSheetFromXML(file.getPath());
                fileNameLabel.setText("Selected file: " + file.getName());
                sheetComponentController.resetSorting();
                // עדכון sheetController לאחר טעינת הקובץ
                sheetComponentController.loadSheetCurrent();



                // רוקן את ה-ComboBox לפני הוספת פריטים חדשים
                versionComboBox.getItems().clear();
                List<Integer> versions = sheetComponentController.getVersionList();
                // עבור על הרשימה ובנה מחרוזות עם מספרי גרסאות ומספרי שינויים
                for (int i = 0; i < versions.size(); i++) {
                    int versionNumber = i + 1; // מספר הגרסה מתחיל מ-1
                    int changes = versions.get(i); // מספר השינויים לאותה גרסה
                    String versionText = "Version " + versionNumber + " - " + changes + " changes";

                    // הוסף את המחרוזת ל-ComboBox
                    versionComboBox.getItems().add(versionText);
                }

                listOfRanges.getItems().addAll(sheetComponentController.getRanges().keySet());

                //populateGrid();
            } catch (Exception e) {
                fileNameLabel.setText("an error occurred." + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // מציג הודעת שגיאה אם לא נבחר קובץ
            fileNameLabel.setText("No file selected or an error occurred.");
        }
    }


}
