package controllerPack;
import dto.CellDto;
import dto.SheetDto;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import sheet.coordinate.api.Coordinate;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;


import java.io.File;

public class Controller {

    private Label selectedLabel = null;
    private Coordinate selectedCoordinate = null;



    @FXML
    private Button LoadButton;

    @FXML
    private Label fileNameLabel;

    @FXML
    private SheetController sheetComponentController;

    //@FXML
    //private ScrollPane sheetScrollPane;

    @FXML
    private TextField cellInputContentTextField;


    @FXML
    private void initialize() {
        // Listener לשינויי הבחירה של התא
        sheetComponentController.selectedCellProperty().addListener((observable, oldLabel, newLabel) -> {
            if (newLabel != null) {
                // כאשר נבחר תא חדש, עדכון תיבת הטקסט עם הערך שלו
                //String cellContent = newLabel.getText();
                cellInputContentTextField.setText(sheetComponentController.getSelectedCoordinateOriginalValue());
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
                }
            }
        });
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
                // עדכון sheetController לאחר טעינת הקובץ
                sheetComponentController.updateSheet();

                //populateGrid();
            } catch (Exception e) {
                fileNameLabel.setText("an error occurred." + e.getMessage());
            }
        } else {
            // מציג הודעת שגיאה אם לא נבחר קובץ
            fileNameLabel.setText("No file selected or an error occurred.");
        }
    }

    /*
    public void populateGrid() {
// משתנה לשמירת התא המסומן הנוכחי

        sheetGridPane.setGridLinesVisible(false); // מכבה את קווי ההפרדה לפני הוספת תאים

        sheetGridPane.getChildren().clear(); // מנקה את GridPane לפני שמוסיפים נתונים חדשים
        sheetGridPane.getRowConstraints().clear(); // מנקה את הגדרת השורות
        sheetGridPane.getColumnConstraints().clear(); // מנקה את הגדרת העמודות
        sheetGridPane.setGridLinesVisible(false); // מכבה את קווי ההפרדה לפני הוספת תאים

        SheetDto currSheet = sheetEngine.getCurrentSheetDTO();

        // גודל קבוע של כל תא
        final double cellWidth = 100.0; // רוחב קבוע לכל תא
        final double cellHeight = 30.0; // גובה קבוע לכל תא

        // קביעת גודל ה-GridPane לפי מספר השורות והעמודות של הגיליון
        for (int row = 0; row < currSheet.getNumOfRows(); row++) {
            for (int col = 0; col < currSheet.getNumOfColumns(); col++) {
                CellDto currCell = currSheet.getCell(row, col);
                Label label;
                if (currCell != null && currCell.getValue() != null && !currCell.getValue().isEmpty()) {
                    String currValue = currCell.getValue();
                    label = new Label(currValue);
                } else {
                    // יצירת תווית ריקה למשבצות ריקות
                    label = new Label("");
                }

                // הגדרת רוחב מקסימלי כדי לחתוך טקסט אם הוא גדול מדי
                label.setMaxWidth(cellWidth);
                label.setWrapText(false); // ביטול גלישת טקסט
                label.setEllipsisString("..."); // הוספת שלוש נקודות במידת הצורך לחיתוך

                // מיקום התווית בתוך התא
                label.setStyle("-fx-alignment: CENTER_LEFT; -fx-background-color: white; -fx-padding: 5px;");

                // הוספת שינוי צבע בעת מעבר עם העכבר
                label.setOnMouseEntered(event -> {
                    label.setStyle("-fx-background-color: lightblue; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                });

                // חזרה לצבע המקורי כאשר העכבר יוצא מהתא
                label.setOnMouseExited(event -> {
                    if (label != selectedLabel) { // אם התווית הנוכחית היא לא זו שסומנה
                        label.setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    }
                });

                // הוספת אירוע לחיצה לסימון תא
                label.setOnMouseClicked(event -> {
                    // אם יש תא מסומן קודם, נסיר ממנו את הסימון
                    if (selectedLabel != null) {
                        selectedLabel.setStyle("-fx-background-color: white; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                    }

                    // נסמן את התא הנוכחי
                    selectedLabel = label;
                    selectedLabel.setStyle("-fx-background-color: #add8e6; -fx-alignment: CENTER_LEFT; -fx-padding: 5px;");
                });

                sheetGridPane.add(label, col, row);
            }
        }

        // הגדרת גודל קבוע של השורות והעמודות
        for (int row = 0; row < currSheet.getNumOfRows(); row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(cellHeight); // גובה קבוע לשורות
            sheetGridPane.getRowConstraints().add(rowConstraints);
        }

        for (int col = 0; col < currSheet.getNumOfColumns(); col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(cellWidth); // רוחב קבוע לעמודות
            sheetGridPane.getColumnConstraints().add(colConstraints);
        }

        sheetGridPane.setGridLinesVisible(true);
    }

     */




}
