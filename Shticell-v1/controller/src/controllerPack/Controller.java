package controllerPack;
import dto.CellDto;
import dto.SheetDto;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
import java.util.Map;
import java.util.stream.Collectors;

public class Controller {

    private Label selectedLabel = null;
    private Coordinate selectedCoordinate = null;
    private List<String> currentColsSelected;
    private boolean isProgrammaticChange = false;

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


    @FXML
    private ComboBox<Object> alignmentBox;

    //@FXML
    //private ScrollPane sheetScrollPane;

    @FXML
    private Accordion accordion;

    @FXML
    private Slider rowHeightSlider; // ה-Slider מה-FXML


    @FXML
    private Slider colWidthSlider; // ה-Slider מה-FXML


    @FXML
    private GridPane hiddenItems; // ה-GridPane שהוספת



    @FXML
    private TextField cellInputContentTextField;

    @FXML
    private TabPane columnTabPane;


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
    private Label LastUpdate;


    @FXML
    private ProgressBar taskProgressBar;


    @FXML
    private ListView<String> colList; // ListView לרשימת העמודות

    private String draggedItem;  // נשמור את האיבר הנגרר


    @FXML
    private ListView<String> listOfRanges; // אותו שם שהשתמשת ב-Scene Builder


    @FXML
    private ColorPicker backgroundPicker;
    @FXML
    private ColorPicker fontPicker;


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
                LastUpdate.setText("Last Update: Version " + sheetComponentController.getLastUpdatedVersion());

                // **עדכון הסליידרים עם רוחב וגובה התא הנבחר**
                double cellWidth = sheetComponentController.getCellWidth();
                double cellHeight = sheetComponentController.getCellHeight();

                isProgrammaticChange = true;
                colWidthSlider.setValue(cellWidth);
                rowHeightSlider.setValue(cellHeight);
                isProgrammaticChange = false;



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

                updateColumnList(sheetComponentController.getSelectedColumns());
                initializeTabsForSelectedColumns(sheetComponentController.getUniqueValuesInRange(topLeft, bottomRight), topLeft, bottomRight);

                // **עדכון הסליידרים עם ממוצע רוחב וגובה התאים בטווח הנבחר**
                double avgCellWidth = sheetComponentController.getAverageCellWidth();
                double avgCellHeight = sheetComponentController.getAverageCellHeight();

                isProgrammaticChange = true;
                colWidthSlider.setValue(avgCellWidth);
                rowHeightSlider.setValue(avgCellHeight);
                isProgrammaticChange = false;

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
// קבלת רשימת ה-String מה-ListView
                ObservableList<String> columnStrings = colList.getItems();

// המרת הרשימה לרשימת Character
                List<Character> columnChars = columnStrings.stream()
                        .map(s -> s.charAt(0))  // קבלת התו הראשון של כל מחרוזת
                        .collect(Collectors.toList());

// קריאה לפונקציית המיון שלך במנוע הגיליון
                sheetComponentController.sortRowsInRange(topLeft, bottomRight, columnChars);
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

        ObservableList<String> emptyList = FXCollections.observableArrayList();
        colList.setItems(emptyList);  // אתחול ה-ListView עם רשימה ריקה
        //addDragEvents(emptyList); // לרשימת מיון






        listOfRanges.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetComponentController.highlightFunctionRange(newValue); // קריאה לפונקציה שמדגישה את הטווח
            }
        });

        enableColumnReordering();


        columnTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        columnTabPane.setSide(Side.TOP); // הטאבים יופיעו בחלק העליון




        colWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isProgrammaticChange) {
                updateColWidth();
            }
        });



        rowHeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isProgrammaticChange) {
                updateRowHeight();
            }
        });





        // הוספת אפשרויות ל-ComboBox
        alignmentBox.getItems().addAll("Left", "Center", "Right");

        // הגדרת ערך ברירת מחדל
        alignmentBox.setValue("Left");



        taskProgressBar.setVisible(false);
    }




    @FXML
    private void changeBackgroundColor() {

        Color color = backgroundPicker.getValue(); // קבלת הצבע שנבחר
        String colorHex = toHexString(color); // המרת הצבע למחרוזת Hex
        sheetComponentController.ChangeBackground(colorHex);
    }

    @FXML
    private void changeTextColor() {
        Color color = fontPicker.getValue(); // קבלת הצבע שנבחר
        String colorHex = toHexString(color); // המרת הצבע למחרוזת Hex
        sheetComponentController.ChangeTextColor(colorHex);
    }


    @FXML
    private void onAlignmentChange() {
        String selectedAlignment = (String)alignmentBox.getValue();
        switch (selectedAlignment) {
            case "Left":
                sheetComponentController.ChangeAlignment("LEFT");
                break;
            case "Center":
                sheetComponentController.ChangeAlignment("CENTER");
                break;
            case "Right":
                sheetComponentController.ChangeAlignment("RIGHT");
                break;
        }
    }


    @FXML
    private void resetStyle() {
        sheetComponentController.resetStyle();
    }


    private String toHexString(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }




    // פונקציה שמופעלת כאשר ה-Slider משתנה
    @FXML
    private void updateColWidth() {
        double newWidth = colWidthSlider.getValue(); // השגת הערך מה-Slider


        sheetComponentController.updateColWidth(newWidth);

    }


    @FXML
    private void updateRowHeight() {
        double newHeight = rowHeightSlider.getValue(); // השגת הערך מה-Slider

        sheetComponentController.updateRowHeight(newHeight);

    }


    // אתחול הטאבים לפי העמודות שנבחרו והכנסת הערכים
    public void initializeTabsForSelectedColumns(Map<String, List<String>> columnData, Coordinate topLeft, Coordinate bottomRight) {
        columnTabPane.getTabs().clear(); // ניקוי הטאבים הקיימים

        for (String columnName : columnData.keySet()) {
            // יצירת טאב חדש לעמודה
            Tab tab = new Tab(columnName);

            // יצירת VBox להחזיק את ה-CheckBoxes עבור כל הערכים בעמודה זו
            VBox vbox = new VBox();

            // מעבר על הערכים הייחודיים של העמודה והוספתם ל-VBox עם CheckBox מסומן
            List<String> uniqueValues = columnData.get(columnName);
            for (String value : uniqueValues) {
                CheckBox checkBox = new CheckBox(value);
                checkBox.setSelected(true); // ברירת מחדל: כל הערכים מסומנים



                checkBox.setOnAction(event -> {
                    if (checkBox.isSelected()) {
                        // כאשר המשתמש מסמן מחדש את הערך, נחזיר את השורות המתאימות
                        sheetComponentController.addRowsForValue(columnName, value, topLeft, bottomRight);
                    }
                    else {
                        sheetComponentController.removeRowsForValue(columnName, value, topLeft, bottomRight);
                    }

                });

                // הוספת CheckBox ל-VBox
                vbox.getChildren().add(checkBox);
            }

            // הוספת ה-VBox לטאב
            tab.setContent(vbox);

            // הוספת הטאב ל-TabPane


            columnTabPane.getTabs().add(tab);


        }



    }





    public void updateColumnList(List<String> columns) {
        colList.getItems().setAll(columns);
    }


    @FXML
    private void deleteSelectedRange() {
        // קבלת הפריט שנבחר מהרשימה
        String selectedRange = listOfRanges.getSelectionModel().getSelectedItem();

        // בדיקה אם יש פריט נבחר
        if (selectedRange != null) {
            // מחיקת הטווח מה-Map של הטווחים
            boolean isDeleted = sheetComponentController.deleteRange(selectedRange);

            if (isDeleted) {
                // הסרת הטווח מהרשימה ב-UI
                listOfRanges.getItems().remove(selectedRange);

            } else {
                // הצגת הודעת אזהרה אם המחיקה לא הצליחה (למשל, אם הטווח בשימוש)
                showAlert("Cannot delete range", "The range is being used by a function.");
            }
        } else {
            // אם לא נבחר טווח, להציג הודעה מתאימה
            showAlert("No range selected", "Please select a range to delete.");
        }

    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }






    // פונקציה לטעינת העמודות לתוך הרשימה
    private void initializeColumnList(List<String> columns) {
        colList.getItems().clear(); // ניקוי הרשימה אם יש פריטים קודמים
        colList.getItems().addAll(columns); // הוספת שמות העמודות
    }


    private void enableColumnReordering() {
        colList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();

            // קישור הטקסט של התאים לנתונים ברשימה
            cell.textProperty().bind(cell.itemProperty());

            // הגדרת גרירה
            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) return;

                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);

                // ביטול כל בחירה כאשר הגרירה מתחילה
                colList.getSelectionModel().clearSelection();

                // יצירת Snapshot של הטקסט הנגרר
                WritableImage snapshot = cell.snapshot(null, null);
                db.setDragView(snapshot);  // הגדרת האות הנגררת במקום סמל הדף

                ClipboardContent cc = new ClipboardContent();
                cc.putString(cell.getItem());
                db.setContent(cc);
                event.consume();
            });

            // קבלת הפריט שנגרר
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);

                    // הוספת אינדיקציה בצבע בזמן גרירה על תא אחר
                    cell.setStyle("-fx-background-color: #bc93a3; -fx-border-color: #cc9daa; -fx-border-width: 2px;");
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

            // החזרת העיצוב המקורי לאחר שהגרירה הסתיימה
            cell.setOnDragExited(event -> {
                if (cell.isSelected()) {
                    // ודא שהסגנון נשאר בצבע החום כאשר הגרירה יוצאת מהתא הנבחר
                    cell.setStyle("-fx-background-color: #8b5e3c; -fx-text-fill: #ffffff;");
                } else {
                    cell.setStyle(""); // חזרה לעיצוב המקורי כאשר הגרירה מסתיימת
                }
            });

            // מחיקת גרירה לאחר סיום
            cell.setOnDragDone(event -> {
                colList.getSelectionModel().clearSelection(); // ביטול הבחירה ברשימה
                cell.setStyle(""); // הסרת הסגנון כאשר הגרירה מסתיימת
                event.consume();
            });

            // שינוי הצבע של השורה בזמן בחירה (לחום ולא כחול)
            cell.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    cell.setStyle("-fx-background-color: #8b5e3c; -fx-text-fill: #ffffff;"); // צבע חום עם טקסט לבן
                } else {
                    cell.setStyle(""); // חזרה לעיצוב המקורי כאשר לא נבחר
                }
            });

            // וידוא שהסגנון נשמר כאשר העכבר נכנס ויוצא מהשורה
            cell.setOnMouseEntered(event -> {
                if (cell.isSelected()) {
                    cell.setStyle("-fx-background-color: #8b5e3c; -fx-text-fill: #ffffff;");
                }
            });

            cell.setOnMouseExited(event -> {
                if (cell.isSelected()) {
                    cell.setStyle("-fx-background-color: #8b5e3c; -fx-text-fill: #ffffff;");
                }
            });

            return cell;
        });
    }




    // פונקציה לרענון התצוגה של הגיליון אחרי המיון
    private void refreshSheetDisplay() {
        sheetComponentController.loadSheetCurrent();
    }

    /*
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
                sheetVersionController.updateSheet(sheetComponentController.getVersionDto(1));


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

     */




    @FXML
    void loadButtonActionListener(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");

        // Adding a filter to allow only XML files (optional)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            // Create a Task to load the file in the background
            Task<Void> loadFileTask = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        // הצגת ה-ProgressBar כשהטעינה מתחילה
                        Platform.runLater(() -> {
                            taskProgressBar.setVisible(true); // הופך את ה-progress bar לגלוי
                            fileNameLabel.setText("Loading: Starting file load...");
                        });

                        // עדכון הדרגתי של הטעינה
                        smoothProgressUpdate(0.0, 0.2, 250); // עדכון ל-20%

                        // טעינת הקובץ
                        sheetComponentController.loadSheetFromFile(file.getPath());
                        Platform.runLater(() -> fileNameLabel.setText("Loading: File loaded successfully."));
                        smoothProgressUpdate(0.2, 0.4, 250); // עדכון ל-40%

                        // איפוס המיון
                        sheetComponentController.resetSorting();
                        Platform.runLater(() -> fileNameLabel.setText("Loading: Resetting sorting..."));
                        smoothProgressUpdate(0.4, 0.6, 250); // עדכון ל-60%

                        // טעינת הגיליון הנוכחי
                        Platform.runLater(() -> {
                            fileNameLabel.setText("Loading: Updating sheet view...");
                            sheetComponentController.loadSheetCurrent();
                            sheetVersionController.updateSheet(sheetComponentController.getVersionDto(1));

                            // עדכון ComboBox וה-Label
                            fileNameLabel.setText("Loading: Populating version list...");
                            versionComboBox.getItems().clear();
                            List<Integer> versions = sheetComponentController.getVersionList();

                            for (int i = 0; i < versions.size(); i++) {
                                int versionNumber = i + 1;
                                int changes = versions.get(i);
                                String versionText = "Version " + versionNumber + " - " + changes + " changes";
                                versionComboBox.getItems().add(versionText);
                            }

                            listOfRanges.getItems().addAll(sheetComponentController.getRanges().keySet());

                            // הסרת הסיומת ".xml" משם הקובץ
                            String fileNameWithoutExtension = file.getName().replaceAll("\\.xml$", "");

                            // הצגת הטקסט של הקובץ שנבחר לאחר כל העדכונים
                            fileNameLabel.setText("Selected file: " + fileNameWithoutExtension);
                        });

                        // סימולציה של סיום הטעינה
                        smoothProgressUpdate(0.6, 1.0, 250);
                        Platform.runLater(() -> {
                            taskProgressBar.setVisible(false); // מסתיר את ה-progress bar לאחר סיום הטעינה
                        });

                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            fileNameLabel.setText("An error occurred: " + e.getMessage());
                            taskProgressBar.setVisible(false); // מסתיר את ה-progress bar במקרה של שגיאה
                        });
                        e.printStackTrace();
                    }
                    return null;
                }



                // פונקציה לעדכון ההתקדמות דרך ה-Task עצמו
                private void smoothProgressUpdate(double startProgress, double endProgress, int durationMillis) throws InterruptedException {
                    int steps = 50; // מספר הצעדים ליצירת אנימציה חלקה
                    double increment = (endProgress - startProgress) / steps;
                    int stepDuration = durationMillis / steps;

                    for (int i = 0; i <= steps; i++) {
                        double progress = startProgress + (increment * i);
                        updateProgress(progress, 1); // עדכון ה-Task עצמו
                        Thread.sleep(stepDuration); // עיכוב קצר בין כל עדכון כדי ליצור אפקט של אנימציה חלקה
                    }
                }
            };



            // Bind the progress bar to the task's progress
            taskProgressBar.progressProperty().bind(loadFileTask.progressProperty());

            // Start the task in a new thread
            Thread thread = new Thread(loadFileTask);
            thread.setDaemon(true);
            thread.start();
        } else {
            // Display an error message if no file is selected
            fileNameLabel.setText("No file selected or an error occurred.");
        }
    }


}
