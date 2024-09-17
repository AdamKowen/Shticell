package controllerPack;
import dto.CellDto;
import dto.SheetDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;


import java.io.File;
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
    private void initialize() {
        // Listener לשינויי הבחירה של התא
        sheetComponentController.selectedCellProperty().addListener((observable, oldLabel, newLabel) -> {
            if (newLabel != null) {
                // עדכון תיבת הטקסט עם הערך של התא הנבחר
                cellInputContentTextField.setText(sheetComponentController.getSelectedCoordinateOriginalValue());

                // הצגת הקואורדינטות של התא הנבחר
                selectedCoordinate = sheetComponentController.getSelectedCoordinate();
                ///selectedCellLabel.setText("Selected cell: " + selectedCoordinate);

                // הגדרת המיקוד על תיבת הטקסט כך שהסמן יהיה בפנים
                cellInputContentTextField.requestFocus();

                // ממקם את הסמן בסוף הטקסט הקיים
                cellInputContentTextField.positionCaret(cellInputContentTextField.getText().length());
            } else {
                cellInputContentTextField.setText("");
                selectedCellLabel.setText("Selected cell: none");
            }
        });


        /*
        // הוספת Listener לטווח תאים
        sheetComponentController.selectedRangeProperty().addListener((observable, oldRange, newRange) -> {
            if (newRange != null) {
                Coordinate topLeft = newRange.getTopLeft();
                Coordinate bottomRight = newRange.getBottomRight();
                selectedCellLabel.setText("Selected range: " + topLeft + " to " + bottomRight);
            } else {
                selectedCellLabel.setText("Selected cell: none");
            }
        });

         */


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

                //populateGrid();
            } catch (Exception e) {
                fileNameLabel.setText("an error occurred." + e.getMessage());
            }
        } else {
            // מציג הודעת שגיאה אם לא נבחר קובץ
            fileNameLabel.setText("No file selected or an error occurred.");
        }
    }


}
