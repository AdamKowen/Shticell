package component.sheetViewfinder;
import com.google.gson.Gson;
import component.main.AppMainController;
import component.sheet.SheetController;
import dto.CellDto;
import dto.SheetDto;
import dto.SheetDtoImpl;
import jakarta.servlet.http.HttpServletResponse;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import okhttp3.*;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import component.cellrange.CellRange;
import util.Constants;
import util.http.HttpClientUtil;
import utils.JSONUtils;


import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static util.Constants.*;

public class SheetViewfinderController {

    private AppMainController appMainController;
    private Label selectedLabel = null;
    private Coordinate selectedCoordinate = null;
    private List<String> currentColsSelected;
    private boolean isProgrammaticChange = false;
    private Coordinate topLeft;
    private Coordinate bottomRight;
    private boolean responsiveMode = false;
    private Timer versionCheckTimer;
    private int currentSheetVersion;

    private boolean readerMode = false;

    private boolean dynamicMode = false;


    @FXML
    private Button LoadButton;

    @FXML
    private Label fileNameLabel;

    @FXML
    private SheetController sheetComponentController;

    @FXML
    private SheetController sheetVersionController;

    @FXML
    private Button resetFiltersButton;

    @FXML
    private ComboBox<Object> versionComboBox;


    @FXML
    private ComboBox<Object> alignmentBox;


    @FXML
    private ToggleButton darkModeToggle;


    @FXML
    private ToggleButton responsiveToggle;

    // תכונה בוליאנית שמייצגת את מצב הרספונסיביות
    private BooleanProperty isResponsive = new SimpleBooleanProperty(false);



    @FXML
    private Accordion accordion;

    @FXML
    private Slider rowHeightSlider; // ה-Slider מה-FXML


    @FXML
    private Slider colWidthSlider; // ה-Slider מה-FXML


    @FXML
    private GridPane hiddenItems; // ה-GridPane שהוספת

    @FXML
    private TextField rangeNameTextBox;



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
    private Button reSelect;

    @FXML
    private Button updateValueButton;


    @FXML
    private Button addOrDeleteRange;

    @FXML
    private Label selectedCellLabel;

    @FXML
    private Label cellUpdateError;

    @FXML
    private Label LastUpdate;


    @FXML
    private Label lastUserUpdatedLabel;


    @FXML
    private Label rangeErrorMassage;


    @FXML
    private Label errorSelectMassage;
    

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
    private TabPane mainTabPane;

    @FXML
    private Tab currentSheetTab;

    @FXML
    private Tab prevSheetTab;

    @FXML
    private VBox topPane;


    @FXML
    private BorderPane sheetViewfinderRootPane;



    private Slider excludedSlider = null; //in movement in dynamic will make sure things dong mess up


    //Dynamic Analysis:

    @FXML
    private TableView<Map.Entry<String, Slider>> sliderTable;

    @FXML
    private TableColumn<Map.Entry<String, Slider>, String> cellNameCol;

    @FXML
    private TableColumn<Map.Entry<String, Slider>, Slider> sliderCol;


    @FXML
    private TextField sliderFromTextfield;

    @FXML
    private TextField sliderToTextfield;

    @FXML
    private ComboBox<Double> stepSizeChoice;

    @FXML
    private Button addSliderButton;

    @FXML
    private Button emptySheetAndResetButton;

    @FXML
    private Label dynamicAnalysisErrorMassage;

    private ObservableList<Map.Entry<String, Slider>> sliderData = FXCollections.observableArrayList();


    @FXML
    private void initialize() {


//

        // Listener לשינויי הבחירה של התא
        sheetComponentController.selectedCellProperty().addListener((observable, oldLabel, newLabel) -> {
            if (newLabel != null) {

                // הצגת הקואורדינטות של התא הנבחר
                selectedCoordinate = sheetComponentController.getSelectedCoordinate();

                // עדכון תיבת הטקסט עם הערך של התא הנבחר
                cellInputContentTextField.setText(sheetComponentController.getSelectedCoordinateOriginalValue());

                selectedCellLabel.setText("Selected cell: " + selectedCoordinate);
                LastUpdate.setText("Last Update: Version " + sheetComponentController.getLastUpdatedVersion());
                lastUserUpdatedLabel.setText("By User: " + sheetComponentController.getLastUserUpdatedCell());

                // **עדכון הסליידרים עם רוחב וגובה התא הנבחר**
                double cellWidth = sheetComponentController.getCellWidth();
                double cellHeight = sheetComponentController.getCellHeight();

                isProgrammaticChange = true;
                colWidthSlider.setValue(cellWidth);
                rowHeightSlider.setValue(cellHeight);
                isProgrammaticChange = false;




                listOfRanges.getSelectionModel().clearSelection();
                rangeNameTextBox.clear();
                addOrDeleteRange.setText("Add Selected");
                rangeErrorMassage.setText("");
                cellUpdateError.setText("");
                errorSelectMassage.setText("");

                // מוודא שהסמן בסוף ומבצע הסרת סימון
                Platform.runLater(() -> {
                    cellInputContentTextField.requestFocus(); // מוודא שהתיבה ממוקדת
                    cellInputContentTextField.positionCaret(cellInputContentTextField.getText().length()); // ממקם את הסמן בסוף
                    cellInputContentTextField.selectRange(cellInputContentTextField.getText().length(), cellInputContentTextField.getText().length()); // מסיר את הסימון
                });





            } else {
                cellInputContentTextField.setText("");
                selectedCellLabel.setText("Selected cell: none");
            }
        });


        // הוספת Listener לטווח תאים
        sheetComponentController.selectedRangeProperty().addListener((observable, oldRange, newRange) -> {
            if (newRange != null) {
                topLeft = newRange.getTopLeft();
                bottomRight = newRange.getBottomRight();
                selectedCellLabel.setText("Selected range: " + sheetComponentController.actualCellPlacedOnGrid(topLeft) + " to " + sheetComponentController.actualCellPlacedOnGrid(bottomRight));
                topLeftBox.setText(sheetComponentController.actualCellPlacedOnGrid(topLeft).toString());
                bottomRightBox.setText(sheetComponentController.actualCellPlacedOnGrid(bottomRight).toString());

                updateColumnList(sheetComponentController.getSelectedColumns());

                initializeTabsForSelectedColumns(sheetComponentController.getUniqueValuesInRange(topLeft, bottomRight), topLeft, bottomRight);

                // **עדכון הסליידרים עם ממוצע רוחב וגובה התאים בטווח הנבחר**
                double avgCellWidth = sheetComponentController.getAverageCellWidth();
                double avgCellHeight = sheetComponentController.getAverageCellHeight();

                isProgrammaticChange = true;
                colWidthSlider.setValue(avgCellWidth);
                rowHeightSlider.setValue(avgCellHeight);
                isProgrammaticChange = false;


                listOfRanges.getSelectionModel().clearSelection();
                rangeNameTextBox.clear();
                addOrDeleteRange.setText("Add Selected");
                rangeErrorMassage.setText("");
                cellUpdateError.setText("");
                errorSelectMassage.setText("");
                LastUpdate.setText("");
                lastUserUpdatedLabel.setText("");

                selectedCoordinate = null; // check if not making problems!!!!!!!!!!!

            } else {
                selectedCellLabel.setText("No range selected");
            }
        });



        // הוספת Listener ללחיצה על Enter בתיבת הטקסט
        cellInputContentTextField.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.ENTER) {
                handleUpdate();
            }

        });




        // הוסף מאזין לבחירה ב-ComboBox
        versionComboBox.setOnAction(event -> {
            // קבל את האינדקס של הבחירה (האינדקס תואם לסדר של הגרסאות)
            int selectedIndex = versionComboBox.getSelectionModel().getSelectedIndex();

            if (selectedIndex != -1) { // ודא שמשהו נבחר
                // טען את הגרסה שנבחרה לפי האינדקס
                //sheetVersionController.updateSheet(sheetComponentController.getVersionDto(selectedIndex+1)); // לדוגמה, טען את הגרסה שנבחרה
                getSheetVersion(selectedIndex+1, sheetDto -> {
                    // טיפול בגרסת הגיליון שהתקבלה, למשל: הצגת הגרסה ב-UI
                    sheetVersionController.setPresentedSheet(sheetDto);
                }, errorMessage -> {
                    // טיפול בשגיאה, הצגת הודעה למשתמש או לוג
                    System.out.println("Error: " + errorMessage);
                });


                // החלף אוטומטית לטאב של הגרסה הקודמת
                mainTabPane.getSelectionModel().select(prevSheetTab);
            }

        });




        ObservableList<String> emptyList = FXCollections.observableArrayList();
        colList.setItems(emptyList);  // אתחול ה-ListView עם רשימה ריקה
        //addDragEvents(emptyList); // לרשימת מיון


        /*
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == currentSheetTab) {
                onCurrentSheetTabSelected();
            }
            // ניתן להוסיף תנאים נוספים אם צריך
        });

         */


        listOfRanges.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetComponentController.highlightFunctionRange(newValue); // קריאה לפונקציה שמדגישה את הטווח
                rangeNameTextBox.setText(newValue);
                rangeErrorMassage.setText("");
                addOrDeleteRange.setText("Delete Range");
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







        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == prevSheetTab) {
                // Disable controls when Previous Version Sheet tab is selected
                setControlsDisabledVersionMode(true);
            } else {
                setControlsDisabledVersionMode(false);
                setUpdatingControlsDisabled(readerMode); //if reader mode elements for updating not active
            }
        });

        sheetVersionController.setReadOnly(true);








        initializeTableColumns();
        initializeStepSizeBox();


        LastUpdate.setText("");
        lastUserUpdatedLabel.setText("");
        //setControlsDisabledAppStart(true);





        // קישור ה-ToggleButton לתכונה הבוליאנית
        responsiveToggle.selectedProperty().bindBidirectional(isResponsive);

        // מאזין לשינויים בערך של isResponsive
        isResponsive.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("Responsive mode is ON");
                updateValueButton.setVisible(false);
                // קוד להפעלת מצב רספונסיבי
            } else {
                System.out.println("Responsive mode is OFF");
                updateValueButton.setVisible(true);
                // קוד לכיבוי מצב רספונסיבי
            }
        });



        // מאזין לשינויים בכפתור
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            appMainController.setDarkMode(newValue); // עדכון מצב darkMode ב-AppMainController
        });


/*
// Listener לשינויים בטקסט של cellInputContentTextField
        cellInputContentTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // בדיקה אם יש תא מסומן (שים כאן את התנאי המתאים שלך)
            if (selectedCoordinate != null && isResponsiveMode()) { // נניח שיש לך משתנה selectedCoordinate שמייצג את התא הנבחר
                // קבלת הטקסט מהתיבה

                try {
                    // הגדרת פרמטרים לבקשת העדכון
                    String coordinate = coordinateToString(selectedCoordinate); // המרה למחרוזת של הקואורדינטה (לדוגמה: "A1")
                    int currentSheetVersion = sheetComponentController.getCurrentSheetVersion(); // קבלת גרסת הגיליון הנוכחית

                    // יצירת הבקשה לעדכון התא
                    sendUpdateRequest(coordinate, newValue, currentSheetVersion);

                } catch (Exception e) {
                    cellUpdateError.setText(e.getMessage());
                }
            }
        });

 */


        // מאזין רק לאירועי הקלדה במקלדת עבור TextField
        cellInputContentTextField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            // בדיקה אם יש תא מסומן (שים כאן את התנאי המתאים שלך)
            if (selectedCoordinate != null && isResponsiveMode()) { // נניח שיש לך משתנה selectedCoordinate שמייצג את התא הנבחר
                // קבלת הטקסט מהתיבה לאחר כל הקלדה
                String newValue = cellInputContentTextField.getText();

                try {
                    // הגדרת פרמטרים לבקשת העדכון
                    String coordinate = coordinateToString(selectedCoordinate); // המרה למחרוזת של הקואורדינטה (לדוגמה: "A1")
                    int currentSheetVersion = sheetComponentController.getCurrentSheetVersion(); // קבלת גרסת הגיליון הנוכחית

                    // יצירת הבקשה לעדכון התא
                    sendUpdateRequest(coordinate, newValue, currentSheetVersion);

                } catch (Exception e) {
                    cellUpdateError.setText(e.getMessage());
                }
            }
        });



    }




    // Cell value update functions:
    @FXML
    private void handleUpdate() {
        String buttonText = updateValueButton.getText();

        if ("Refresh".equals(buttonText)) { //based on current button function
            refreshSheet();
            if (readerMode)
            {
                updateValueButton.setDisable(true);
            }
            else {
                setUpdatingControlsDisabled(false);
            }
        } else{
            // קבלת התא הנבחר
            Coordinate selectedCoordinate = sheetComponentController.getSelectedCoordinate();

            // קבלת הטקסט מהתיבה
            String newValue = cellInputContentTextField.getText().trim();

            // בדיקה אם יש תא נבחר וטקסט לא ריק
            if (selectedCoordinate != null) {
                try {
                    // הגדרת פרמטרים לבקשת העדכון
                    String coordinate = coordinateToString(selectedCoordinate); // המרה למחרוזת של הקואורדינטה (לדוגמה: "A1")
                    int currentSheetVersion = sheetComponentController.getCurrentSheetVersion(); // קבלת גרסת הגיליון הנוכחית

                    // יצירת הבקשה לעדכון התא
                    sendUpdateRequest(coordinate, newValue, currentSheetVersion);

                } catch (Exception e) {
                    cellUpdateError.setText(e.getMessage());
                }
            } else {
                // אין מה לעדכן - אפשר לבחור להציג הודעה או פשוט לעשות כלום
                cellUpdateError.setText("No cell selected");
            }
        }
    }

    private void sendUpdateRequest(String coordinate, String newValue, int currentSheetVersion) {
        // יצירת הגוף של בקשת ה-POST
        RequestBody formBody = new FormBody.Builder()
                .add("coordinate", coordinate)
                .add("newValue", newValue)
                .add("sheetVersion", String.valueOf(currentSheetVersion))
                .build();

        Request request = new Request.Builder()
                .url(Constants.UPDATE_CELL_URL) // ה-URL של ה-Servlet לעדכון תא
                .post(formBody)
                .build();

        // שליחת הבקשה באופן אסינכרוני
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> cellUpdateError.setText("Failed to update cell: " + e.getMessage()));

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        cellUpdateError.setText("Cell updated successfully.");
                        refreshSheet();
                        refreshVersionComboBox();
                    });
                } else if (response.code() == HttpServletResponse.SC_CONFLICT) {
                    Platform.runLater(() -> cellUpdateError.setText("Version conflict: Please refresh the sheet."));
                } else {
                    // קריאה לגוף התגובה כדי לקבל את הודעת השגיאה מהשרת
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                    Platform.runLater(() -> cellUpdateError.setText(errorMessage));
                }
                response.close();
            }
        });
    }


    private void setUpdatingControlsDisabled(boolean disabled) {

        if(disabled == false)
        {
            if(dynamicMode == false)
                cellInputContentTextField.setDisable(disabled);
        }
        else {
            cellInputContentTextField.setDisable(disabled);
        }
        alignmentBox.setDisable(disabled);
        addOrDeleteRange.setDisable(disabled);
        backgroundPicker.setDisable(disabled);
        fontPicker.setDisable(disabled);
        listOfRanges.setDisable(disabled);
        rangeNameTextBox.setDisable(disabled);

        if (!updateValueButton.getText().equals("Refresh")) {
            updateValueButton.setDisable(disabled);
        }
    }



    public void setReaderMode(boolean isReaderMode) {
        this.readerMode = isReaderMode;
        disableEditingElements(readerMode);  // חוסם את האלמנטים לפי מצב הקריאה
    }

    private void disableEditingElements(boolean isReaderMode) {
        alignmentBox.setDisable(isReaderMode);
        rangeNameTextBox.setDisable(isReaderMode);
        if (isReaderMode == false) {
            if(dynamicMode == false)
                cellInputContentTextField.setDisable(isReaderMode);
        }
        else {
            cellInputContentTextField.setDisable(isReaderMode);
        }
        updateValueButton.setDisable(isReaderMode);
        addOrDeleteRange.setDisable(isReaderMode);
        backgroundPicker.setDisable(isReaderMode);
        fontPicker.setDisable(isReaderMode);

    }




    // Version functions:
    // פונקציה לרענון ה-ComboBox של הגרסאות
    private void refreshVersionComboBox() {
        versionComboBox.getItems().clear();
        List<Integer> versions = sheetComponentController.getVersionList();

        for (int i = 0; i < versions.size(); i++) {
            int versionNumber = i + 1; // מספר הגרסה מתחיל מ-1
            int changes = versions.get(i); // מספר השינויים לאותה גרסה
            String versionText = "Version " + versionNumber + " - " + changes + " changes";
            versionComboBox.getItems().add(versionText);
        }

        versionComboBox.getSelectionModel().clearSelection();
        versionComboBox.setValue(null);
    }







    // Selection:
    @FXML
    private void reSelectAction() {
        try {
            sheetComponentController.reSelect(topLeftBox.getText(), bottomRightBox.getText());
        } catch (Exception e) {
            errorSelectMassage.setText(e.getMessage());
        }
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





    // Range:
    @FXML
    private void handleAddOrDeleteRange() {
        String buttonText = addOrDeleteRange.getText();
        String rangeName = rangeNameTextBox.getText().trim();

        if (rangeName.isEmpty()) {
            // If the range name is empty, show an error
            rangeErrorMassage.setText("Please enter a valid range name.");
            return;
        }

        try {
            if (buttonText.equals("Delete Range")) {
                // Call the delete range function
                deleteRange(rangeName);
                ObservableList<String> rangesList = listOfRanges.getItems();
                rangesList.clear();
                listOfRanges.getItems().addAll(sheetComponentController.getRanges().keySet());
                //rangeErrorMassage.setText("Range '" + rangeName + "' deleted");
                refreshSheet();
                updateListOfRanges();

            } else if (buttonText.equals("Add Selected")) {
                // Call the add range function with the selected coordinates
                addRange(rangeName, sheetComponentController.getTopLeft() , sheetComponentController.getBottomRight());
                //rangeErrorMassage.setText("Range '" + rangeName + "' added successfully.");
                listOfRanges.getItems().addAll(rangeName);
                refreshSheet();
                updateListOfRanges();

            }
        } catch (Exception e) {
            // Handle exceptions and display error messages
            rangeErrorMassage.setText("Error: " + e.getMessage());
        }
    }

    public void updateListOfRanges() {
        // מוחק את כל האיברים הקיימים ברשימה
        listOfRanges.getItems().clear();

        // מוסיף את האיברים החדשים לרשימה
        listOfRanges.getItems().addAll(sheetComponentController.getRanges().keySet());
    }

    // פונקציה למחיקת טווח (Range) לפי שם
    public void deleteRange(String rangeName) {
        // יצירת גוף הבקשה עם שם הטווח
        RequestBody body = new FormBody.Builder()
                .add("rangeName", rangeName)
                .build();

        // יצירת בקשת HTTP לשרת
        Request request = new Request.Builder()
                .url(DELETE_RANGE_URL)
                .post(body)
                .build();

        // שליחת הבקשה לשרת בצורה אסינכרונית
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // במקרה של כשל בבקשה - נעדכן את ממשק המשתמש
                Platform.runLater(() -> {
                    // הצגת הודעת שגיאה
                    rangeErrorMassage.setText("Failed to delete range: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // במקרה של הצלחה או כשל, נבדוק את תגובת השרת
                String message;
                if (response.isSuccessful()) {
                    message = "Range '" + rangeName + "' deleted successfully.";
                } else {
                    message = response.body().string();
                }

                Platform.runLater(() -> {
                    // עדכון תווית rangeErrorMassage עם ההודעה המתאימה
                    rangeErrorMassage.setText(message);
                });
            }
        });
    }

    // פונקציה להוספת טווח (Range) חדש לפי שם וקואורדינטות
    public void addRange(String rangeName, String topLeft, String bottomRight) {
        // יצירת גוף הבקשה עם שם הטווח והקואורדינטות
        RequestBody body = new FormBody.Builder()
                .add("rangeName", rangeName)
                .add("topLeft", topLeft)
                .add("bottomRight", bottomRight)
                .build();

        // יצירת בקשת HTTP לשרת
        Request request = new Request.Builder()
                .url(ADD_RANGE_URL)
                .post(body)
                .build();

        // שליחת הבקשה לשרת בצורה אסינכרונית
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // במקרה של כשל בבקשה - נעדכן את ממשק המשתמש
                Platform.runLater(() -> {
                    // הצגת הודעת שגיאה
                    rangeErrorMassage.setText("Failed to add range: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // במקרה של הצלחה או כשל, נבדוק את תגובת השרת
                String message;
                if (response.isSuccessful()) {
                    message = "Range '" + rangeName + "' added successfully.";
                } else {
                    message = response.body().string();
                }

                Platform.runLater(() -> {
                    // עדכון תווית rangeErrorMassage עם ההודעה המתאימה
                    rangeErrorMassage.setText(message);
                });
            }
        });
    }






    // Sorting actions:
    @FXML
    private void handleSortButton() {
        {
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
        }
    }

    @FXML
    private void handleResetSortButton() {
        sheetComponentController.resetSorting();
        refreshSheetDisplay();
    }

    public void updateColumnList(List<String> columns) {
        colList.getItems().setAll(columns);
    }






    // Cell style actions:

    @FXML
    public void ChangeBackground(String colorHex) {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();

        // קריאה לפונקציה ב-sheetViewfinder עם סוג הסטייל המתאים
        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "backgroundColor", colorHex);
    }

    @FXML
    public void ChangeTextColor(String colorHex) {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();

        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "textColor", colorHex);
    }

    @FXML
    public void ChangeAlignment(String alignment) {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();

        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "alignment", alignment);
    }

    @FXML
    public void resetStyle() {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();

        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "reset", "");
    }

    private String toHexString(Color color) {
        int red = (int) (color.getRed() * 255);
        int green = (int) (color.getGreen() * 255);
        int blue = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }
    public void sendUpdateCellsStyleRequest(List<String> columns, List<Integer> rows, String styleType, String styleValue) {
        String url = Constants.UPDATE_CELLS_STYLE_URL;

        // המרת הרשימות ל-JSON כדי לשלוח כפרמטרים
        Gson gson = new Gson();
        String columnsJson = gson.toJson(columns);
        String rowsJson = gson.toJson(rows);

        RequestBody formBody = new FormBody.Builder()
                .add("styleType", styleType)
                .add("styleValue", styleValue)
                .add("columns", columnsJson)
                .add("rows", rowsJson)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() -> System.out.println("Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Platform.runLater(() -> {
                            System.out.println("Style updated successfully.");
                            refreshSheet();
                            refreshVersionComboBox();
                        });
                    } else {
                        Platform.runLater(() -> System.out.println("Failed to update style. Response code: " + response.code()));
                    }
                } finally {
                    response.close();  // סגירת ה-Response כדי למנוע דליפה
                }
            }
        });
    }


    @FXML
    private void changeBackgroundColor() {

        Color color = backgroundPicker.getValue(); // קבלת הצבע שנבחר
        String colorHex = toHexString(color); // המרת הצבע למחרוזת Hex
        ChangeBackground(colorHex);
    }

    @FXML
    private void changeTextColor() {
        Color color = fontPicker.getValue(); // קבלת הצבע שנבחר
        String colorHex = toHexString(color); // המרת הצבע למחרוזת Hex
        ChangeTextColor(colorHex);
    }

    @FXML
    private void onAlignmentChange() {
        String selectedAlignment = (String)alignmentBox.getValue();
        switch (selectedAlignment) {
            case "Left":
                ChangeAlignment("LEFT");
                break;
            case "Center":
                ChangeAlignment("CENTER");
                break;
            case "Right":
                ChangeAlignment("RIGHT");
                break;
        }
    }


/*
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

 */




    // Filter actions:
    @FXML
    private void handleResetFilters() {
        // מעבר על כל הטאבים במערכת הסינון
        for (Tab tab : columnTabPane.getTabs()) {
            // קבלת ה-ScrollPane מתוך הטאב
            ScrollPane scrollPane = (ScrollPane) tab.getContent();

            // קבלת ה-VBox מתוך ה-ScrollPane
            VBox vbox = (VBox) scrollPane.getContent();

            // מעבר על כל ה-CheckBoxes בתוך ה-VBox
            for (Node node : vbox.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) node;

                    // אם ה-CheckBox אינו מסומן, נסמן אותו ונתחיל את הסינון מחדש
                    if (!checkBox.isSelected()) {
                        checkBox.setSelected(true);

                        // נקרא לפונקציה שמחזירה את השורות המתאימות
                        sheetComponentController.addRowsForValue(tab.getText(), checkBox.getText(), topLeft, bottomRight);
                    }
                }
            }
        }

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
                    } else {
                        sheetComponentController.removeRowsForValue(columnName, value, topLeft, bottomRight);
                    }
                });

                // הוספת CheckBox ל-VBox
                vbox.getChildren().add(checkBox);
            }

            // הוספת VBox ל-ScrollPane כדי לאפשר גלילה
            ScrollPane scrollPane = new ScrollPane(vbox);
            scrollPane.setFitToWidth(true); // מוודא שה-ScrollPane יתאים לרוחב

            // הוספת ה-ScrollPane לטאב
            tab.setContent(scrollPane);

            // הוספת הטאב ל-TabPane
            columnTabPane.getTabs().add(tab);
        }
    }






    // Rows and Cols actions:
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








    // Dynamic Analysis

    private void initializeTableColumns() {
        // הגדרת עמודת שם התא
        cellNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        // הגדרת עמודת ה-Slider
        sliderCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getValue()));

        // הגדרת מקור הנתונים של הטבלה
        sliderTable.setItems(sliderData);
    }

    /*
    @FXML
    private void addSliderForCell() {
        // קריאת ערכים מה-TextField
        String fromText = sliderFromTextfield.getText();
        String toText = sliderToTextfield.getText();

        // בדיקת תקינות הערכים
        if (!isNumeric(fromText) || !isNumeric(toText)) {
            dynamicAnalysisErrorMassage.setText("Please enter numeric values only.");
            return;
        }

        double fromValue = Double.parseDouble(fromText);
        double toValue = Double.parseDouble(toText);
        double stepSize = stepSizeChoice.getSelectionModel().getSelectedItem();

        // בדיקת טווח ו-step size
        if (!isValidRange(fromValue, toValue, stepSize)) {
            dynamicAnalysisErrorMassage.setText("Range is not large enough for the selected step size.");
            return;
        }

        // הסרת הודעת השגיאה
        dynamicAnalysisErrorMassage.setText("");

        // שמירת הקואורדינטה של התא הנבחר במשתנה מקומי
        String cellCoordinate = coordinateToString(selectedCoordinate);

        // יצירת סליידר חדש
        Slider newSlider = new Slider(fromValue, toValue, (fromValue + toValue) / 2);

        // הגדרת stepSize על הסליידר
        newSlider.setBlockIncrement(stepSize);
        newSlider.setMajorTickUnit(stepSize);
        newSlider.setMinorTickCount(0);
        newSlider.setSnapToTicks(true);

        // משתנה דגל פנימי עבור הסליידר המסוים בלבד
        final boolean[] isUserAction = {false};

        // מאזינים להתחלת ונעילת אינטראקציה על הסליידר
        newSlider.setOnMousePressed(event -> isUserAction[0] = true);
        newSlider.setOnMouseReleased(event -> isUserAction[0] = false);

        // שמירה של הערך הקודם כדי לוודא שהעדכון קורה רק בעת שינוי אמיתי
        final double[] lastSentValue = {newSlider.getValue()};

        // מאזין לשינוי ערך הסליידר כאשר המשתמש גורר אותו
        newSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUserAction[0]) {
                // עיגול הערך בהתאם ל-stepSize
                double roundedValue = Math.round(newVal.doubleValue() / stepSize) * stepSize;

                // שליחת עדכון רק אם הערך השתנה למדרגה חדשה לפי ה-stepSize
                if (roundedValue != lastSentValue[0]) {
                    lastSentValue[0] = roundedValue; // עדכון הערך האחרון שנשלח
                    String newValueStr = Double.toString(roundedValue);

                    updateCellInTemporarySheet(cellCoordinate, newValueStr, sheetDto -> {
                        sheetComponentController.setPresentedSheet(sheetDto);
                        updateSlidersPosition();
                        System.out.println("Updated sheet DTO to version: " + sheetDto.getVersion());

                        // עדכון רשימות נוספות בממשק
                        updateListOfRanges();
                        refreshVersionComboBox();
                    }, errorMessage -> {
                        System.out.println("Error updating cell: " + errorMessage);
                    });
                }
            }
        });

        // קביעת ערך התחלתי של הסליידר לפי ערך התא
        String value = sheetComponentController.getCellValue(cellCoordinate);

        if (isNumeric(value)) {
            double cellValue = Double.parseDouble(value);
            if (cellValue >= fromValue && cellValue <= toValue) {
                newSlider.setValue(cellValue);
            } else if (cellValue < fromValue) {
                newSlider.setValue(fromValue);
            } else {
                newSlider.setValue(toValue);
            }
        } else {
            newSlider.setValue((fromValue + toValue) / 2);
        }

        // הוספת שם התא והסליידר לטבלה
        sliderData.add(new AbstractMap.SimpleEntry<>(cellCoordinate, newSlider));
    }

    private void updateSlidersPosition() {
        for (Map.Entry<String, Slider> entry : sliderData) {
            String cellCoordinate = entry.getKey();  // קבלת הקואורדינטה של התא
            Slider slider = entry.getValue();  // קבלת הסליידר

            // קבלת הערך הנוכחי של התא לפי הקואורדינטה
            String cellValue = sheetComponentController.getCellValue(cellCoordinate);

            if (isNumeric(cellValue)) {
                double cellValueDouble = Double.parseDouble(cellValue);

                // בדיקה אם הערך נמצא בטווח של הסליידר
                if (cellValueDouble >= slider.getMin() && cellValueDouble <= slider.getMax()) {
                    slider.setValue(cellValueDouble);
                } else if (cellValueDouble < slider.getMin()) {
                    slider.setValue(slider.getMin());
                } else {
                    slider.setValue(slider.getMax());
                }
            } else {
                // אם הערך אינו מספרי, ממקמים את הסמן באמצע הטווח
                slider.setValue((slider.getMin() + slider.getMax()) / 2);
            }
        }
    }

     */

    @FXML
    private void addSliderForCell() {
        // קריאת ערכים מה-TextField
        String fromText = sliderFromTextfield.getText();
        String toText = sliderToTextfield.getText();

        // בדיקת תקינות הערכים
        if (!isNumeric(fromText) || !isNumeric(toText)) {
            dynamicAnalysisErrorMassage.setText("Please enter numeric values only.");
            return;
        }

        double fromValue = Double.parseDouble(fromText);
        double toValue = Double.parseDouble(toText);
        double stepSize = stepSizeChoice.getSelectionModel().getSelectedItem();

        // בדיקת טווח ו-step size
        if (!isValidRange(fromValue, toValue, stepSize)) {
            dynamicAnalysisErrorMassage.setText("Range is not large enough for the selected step size.");
            return;
        }

        // הסרת הודעת השגיאה
        dynamicAnalysisErrorMassage.setText("");

        // שמירת הקואורדינטה של התא הנבחר במשתנה מקומי
        String cellCoordinate = coordinateToString(selectedCoordinate);

        // יצירת סליידר חדש
        Slider newSlider = new Slider(fromValue, toValue, (fromValue + toValue) / 2);

        // הגדרת stepSize על הסליידר
        newSlider.setBlockIncrement(stepSize);
        newSlider.setMajorTickUnit(stepSize);
        newSlider.setMinorTickCount(0);
        newSlider.setSnapToTicks(true);

        // משתנה דגל פנימי לסימון האם המשתמש כרגע מזיז את הסליידר
        final boolean[] isUserAction = {false};

        // מאזינים להתחלת ונעילת אינטראקציה על הסליידר
        newSlider.setOnMousePressed(event -> {
            isUserAction[0] = true;
            dynamicMode = true;
            updateValueButton.setVisible(false);
            setInActive(); // stopping automatic updating so it doesnt interfere with dynamic mode
            cellInputContentTextField.setDisable(true);
            excludedSlider = newSlider; // הגדרת הסליידר כ-excluded כשהמשתמש מתחיל להזיז
        });

        newSlider.setOnMouseReleased(event -> {
            isUserAction[0] = false;
            excludedSlider = null; // איפוס excludedSlider לאחר שהמשתמש שחרר את העכבר
        });

        // שמירה של הערך הקודם כדי לוודא שהעדכון קורה רק בעת שינוי אמיתי
        final double[] lastSentValue = {newSlider.getValue()};

        // מאזין לשינוי ערך הסליידר כאשר המשתמש גורר אותו
        newSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUserAction[0]) { // נוודא שרק בלחיצה ישירה על הסליידר הבקשה תישלח
                double roundedValue = Math.round(newVal.doubleValue() / stepSize) * stepSize;

                if (roundedValue != lastSentValue[0]) {
                    lastSentValue[0] = roundedValue; // עדכון הערך האחרון שנשלח
                    String newValueStr = Double.toString(roundedValue);

                    updateCellInTemporarySheet(cellCoordinate, newValueStr, sheetDto -> {
                        sheetComponentController.setPresentedSheet(sheetDto);
                        updateSlidersPosition(); // מעדכן את כל הסליידרים חוץ מהסליידר שהמשתמש מזיז
                        System.out.println("Updated sheet DTO to version: " + sheetDto.getVersion());

                        // עדכון רשימות נוספות בממשק
                        updateListOfRanges();
                        refreshVersionComboBox();
                    }, errorMessage -> {
                        System.out.println("Error updating cell: " + errorMessage);
                    });
                }
            }
        });

        // קביעת ערך התחלתי של הסליידר לפי ערך התא
        String value = sheetComponentController.getCellValue(cellCoordinate);

        if (isNumeric(value)) {
            double cellValue = Double.parseDouble(value);
            if (cellValue >= fromValue && cellValue <= toValue) {
                newSlider.setValue(cellValue);
            } else if (cellValue < fromValue) {
                newSlider.setValue(fromValue);
            } else {
                newSlider.setValue(toValue);
            }
        } else {
            newSlider.setValue((fromValue + toValue) / 2);
        }

        // הוספת שם התא והסליידר לטבלה
        sliderData.add(new AbstractMap.SimpleEntry<>(cellCoordinate, newSlider));
    }

    // פונקציה שמעדכנת את כל הסליידרים למעט הסליידר שהמשתמש מזיז
    private void updateSlidersPosition() {
        for (Map.Entry<String, Slider> entry : sliderData) {
            Slider slider = entry.getValue();
            if (slider != excludedSlider) {  // אם הסליידר לא זה שהמשתמש מזיז
                String cellCoordinate = entry.getKey();
                String cellValue = sheetComponentController.getCellValue(cellCoordinate);

                if (isNumeric(cellValue)) {
                    double cellValueDouble = Double.parseDouble(cellValue);
                    if (cellValueDouble >= slider.getMin() && cellValueDouble <= slider.getMax()) {
                        slider.setValue(cellValueDouble);
                    } else if (cellValueDouble < slider.getMin()) {
                        slider.setValue(slider.getMin());
                    } else {
                        slider.setValue(slider.getMax());
                    }
                } else {
                    slider.setValue((slider.getMin() + slider.getMax()) / 2);
                }
            }
        }
    }



    private boolean isValidRange(double fromValue, double toValue, double stepSize) {
        return (fromValue < toValue) && ((toValue - fromValue) >= (5 * stepSize));
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void clearErrorOnInteraction() {
        sliderFromTextfield.setOnKeyTyped(e -> dynamicAnalysisErrorMassage.setText(""));
        sliderToTextfield.setOnKeyTyped(e -> dynamicAnalysisErrorMassage.setText(""));
        stepSizeChoice.setOnAction(e -> dynamicAnalysisErrorMassage.setText(""));
    }

    private void initializeStepSizeBox() {
        // אתחול ComboBox עם ערכים קבועים
        stepSizeChoice.getItems().addAll(100.0, 10.0, 1.0, 0.1, 0.01, 0.001, 0.5, 0.05, 0.005);

        // אפשר להגדיר ערך ברירת מחדל, אם רוצים
        stepSizeChoice.setValue(1.0); // ערך ברירת מחדל של 1
    }


    // פונקציה אסינכרונית שמבצעת בקשת POST לעדכון תא ומחזירה את ה-SheetDto המעודכן
    private void updateCellInTemporarySheet(String cellId, String newValue, Consumer<SheetDto> onSuccess, Consumer<String> onError) {
        // יצירת URL לבקשה
        HttpUrl.Builder urlBuilder = HttpUrl.parse(UPDATE_TEMP_SHEET_URL).newBuilder();
        String finalUrl = urlBuilder.build().toString();

        // יצירת גוף הבקשה עם הפרמטרים cellId ו-newValue
        RequestBody formBody = new FormBody.Builder()
                .add("cellId", cellId)
                .add("newValue", newValue)
                .build();

        // יצירת בקשת POST עם URL וגוף הבקשה
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(formBody)
                .build();

        // שליחת הבקשה באופן אסינכרוני
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // במקרה של כשל - קרא לפונקציית השגיאה
                Platform.runLater(() -> onError.accept("Error updating cell: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // המרת התגובה ל-SheetDto באמצעות JSONUtils
                    String responseBody = response.body().string();
                    SheetDto sheetDto = JSONUtils.fromJson(responseBody, SheetDtoImpl.class);

                    // קריאה לפונקציית ההצלחה ב-UI Thread
                    Platform.runLater(() ->
                    {
                        onSuccess.accept(sheetDto);
                        sheetComponentController.setPresentedSheet(sheetDto);
                        updateSlidersPosition();
                    });
                } else {
                    // קריאה לפונקציית השגיאה אם הבקשה נכשלה
                    String errorMessage = "Failed to update cell. Response code: " + response.code();
                    Platform.runLater(() -> onError.accept(errorMessage));
                }
            }
        });
    }


















    private void setControlsDisabledVersionMode(boolean disabled) {

        if (disabled == false)
        {
            if (!dynamicMode) //brings back the text field only if not in dynamic mode
            {
                cellInputContentTextField.setDisable(disabled);
            }
        }
        else {
            cellInputContentTextField.setDisable(disabled);
        }
        alignmentBox.setDisable(disabled);
        rowHeightSlider.setDisable(disabled);
        colWidthSlider.setDisable(disabled);
        addOrDeleteRange.setDisable(disabled);
        sort.setDisable(disabled);
        resetsort.setDisable(disabled);
        backgroundPicker.setDisable(disabled);
        fontPicker.setDisable(disabled);
        colList.setDisable(disabled);
        listOfRanges.setDisable(disabled);
        rangeNameTextBox.setDisable(disabled);
        topLeftBox.setDisable(disabled);
        bottomRightBox.setDisable(disabled);
        accordion.setDisable(disabled);
        columnTabPane.setDisable(disabled);
        updateValueButton.setDisable(disabled);
        reSelect.setDisable(disabled);
        // Ensure the versionComboBox remains enabled
        versionComboBox.setDisable(false);
    }


/*
    private void setControlsDisabledAppStart(boolean disabled) {
        cellInputContentTextField.setDisable(disabled);
        alignmentBox.setDisable(disabled);
        rowHeightSlider.setDisable(disabled);
        colWidthSlider.setDisable(disabled);
        addOrDeleteRange.setDisable(disabled);
        sort.setDisable(disabled);
        resetsort.setDisable(disabled);
        backgroundPicker.setDisable(disabled);
        fontPicker.setDisable(disabled);
        colList.setDisable(disabled);
        listOfRanges.setDisable(disabled);
        rangeNameTextBox.setDisable(disabled);
        topLeftBox.setDisable(disabled);
        bottomRightBox.setDisable(disabled);
        accordion.setDisable(disabled);
        columnTabPane.setDisable(disabled);
        updateValueButton.setDisable(disabled);
        reSelect.setDisable(disabled);
        mainTabPane.setDisable(disabled);
        versionComboBox.setDisable(disabled);
    }

 */











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


    public void setAppMainController(AppMainController appMainControll) {
        this.appMainController = appMainControll;

        // התאמת המצב של Toggle Button לפי מצב darkMode
        darkModeToggle.setSelected(appMainController.isDarkMode());

        // מאזין לשינויים בכפתור
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            appMainController.setDarkMode(newValue); // עדכון מצב darkMode ב-AppMainController
        });
    }

    public void setSheet(String sheetName) {
        System.out.println("Displaying sheet: " + sheetName);
        fileNameLabel.setText(sheetName);

        try {
            setCurrentSheet(sheetName, log -> System.out.println(log)); // שליחת הבקשה לעדכון
        } catch (IOException e) {
            e.printStackTrace();
        }

        // בקשה אסינכרונית לקבלת ה-SheetDto המעודכן
        getCurrentSheet(sheetDto -> {
            if (sheetDto != null) {
                System.out.println("Sheet loaded successfully: " + sheetName);
                sheetComponentController.setPresentedSheet(sheetDto);
                updateListOfRanges();
                refreshVersionComboBox();
                currentSheetVersion = sheetComponentController.getCurrentSheetVersion();

                // התחלת הבדיקה רק לאחר טעינת ה-SheetDto
                setActive();
            }
        }, errorMessage -> {
            System.out.println("Error: " + errorMessage);
        });
    }


    @FXML
    private void goBackToSheetList() {
        appMainController.switchToAccountArea();  // חזרה למסך הרשימה
    }


    // הפונקציה שמבצעת את בקשת ה-POST לשרת
    public static void setCurrentSheet(String sheetName, Consumer<String> httpRequestLogger) throws IOException {
        // יצירת הבקשה
        RequestBody formBody = new FormBody.Builder()
                .add("sheetName", sheetName)  // הוספת הפרמטר SheetName
                .build();

        Request request = new Request.Builder()
                .url(SET_SHEET_URL)
                .post(formBody)
                .build();

        // שליחת הבקשה באופן אסינכרוני (לא חוסם את ה-UI)
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpRequestLogger.accept("Error setting current sheet: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (response) {  // שימוש ב-try-with-resources כדי לוודא שהתגובה תיסגר אוטומטית
                    if (response.isSuccessful()) {
                        httpRequestLogger.accept("Successfully set current sheet to: " + sheetName);
                    } else {
                        httpRequestLogger.accept("Failed to set current sheet. Response code: " + response.code());
                    }
                }
            }
        });
    }


    // פונקציה אסינכרונית שמבצעת בקשת GET ומקבלת את ה-SheetDto
    public void getCurrentSheet(Consumer<SheetDto> onSuccess, Consumer<String> onError) {
        String finalUrl = HttpUrl
                .parse(String.valueOf(URI.create(Constants.SHEET_URL)))
                .toString();

        // יצירת בקשת GET
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // שליחת הבקשה באופן אסינכרוני
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // במקרה של כשל - קרא לפונקציית השגיאה
                Platform.runLater(() -> onError.accept("Error fetching sheet: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // המרת התגובה ל-SheetDto באמצעות JSONUtils
                    String responseBody = response.body().string();
                    SheetDto sheetDto = JSONUtils.fromJson(responseBody, SheetDtoImpl.class);

                    // קריאה לפונקציית ההצלחה ב-UI Thread
                    Platform.runLater(() -> onSuccess.accept(sheetDto));
                } else {
                    // קריאה לפונקציית השגיאה אם הבקשה נכשלה
                    String errorMessage = "Failed to fetch current sheet. Response code: " + response.code();
                    Platform.runLater(() -> onError.accept(errorMessage));
                }
            }
        });
    }


    private void refreshSheet() {
        // בקשת ה-sheetDTO מהשרת
        getCurrentSheet(sheetDto -> {
            sheetComponentController.setPresentedSheet(sheetDto);

            //listOfRanges.getItems().addAll(sheetComponentController.getRanges().keySet());
            updateListOfRanges();
            refreshVersionComboBox();
            updateSlidersPosition();

            currentSheetVersion = sheetComponentController.getCurrentSheetVersion();
            // החזרת הכפתור למצב "Update"
            updateValueButton.setText("Update");

            System.out.println("Updated sheet to version: " + sheetDto.getVersion());

        }, errorMessage -> {
            System.out.println("Error refreshing sheet: " + errorMessage);
        });
    }



    public void getSheetVersion(int version, Consumer<SheetDto> onSuccess, Consumer<String> onError) {
        String finalUrl = HttpUrl
                .parse(String.valueOf(URI.create(Constants.GET_SHEET_VERSION_URL)))
                .newBuilder()
                .addQueryParameter("version", String.valueOf(version)) // הוספת מספר הגרסה כפרמטר
                .toString();

        // יצירת בקשת GET
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // שליחת הבקשה באופן אסינכרוני
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // במקרה של כשל - קרא לפונקציית השגיאה
                Platform.runLater(() -> onError.accept("Error fetching sheet version: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // המרת התגובה ל-SheetDto באמצעות JSONUtils
                    String responseBody = response.body().string();
                    SheetDto sheetDto = JSONUtils.fromJson(responseBody, SheetDtoImpl.class);

                    // קריאה לפונקציית ההצלחה ב-UI Thread
                    Platform.runLater(() -> onSuccess.accept(sheetDto));
                } else {
                    // קריאה לפונקציית השגיאה אם הבקשה נכשלה
                    String errorMessage = "Failed to fetch sheet version. Response code: " + response.code();
                    Platform.runLater(() -> onError.accept(errorMessage));
                }
            }
        });
    }




    public void startVersionCheck() {
        versionCheckTimer = new Timer(true);
        versionCheckTimer.schedule(new SheetVersionRefresher(this::getCurrentLocalSheetVersion, this::handleVersionCheck), Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    // פונקציה שמחזירה את הגרסה הנוכחית
    private int getCurrentLocalSheetVersion() {
        return sheetComponentController.getCurrentSheetVersion(); // קבלת הגרסה הנוכחית מהאלמנט
    }

    // עצירה של הבדיקה
    public void stopVersionCheck() {
        if (versionCheckTimer != null) {
            versionCheckTimer.cancel();
            versionCheckTimer = null;
        }
    }

    // פונקציה שתיקרא כאשר יש גרסה חדשה - לפי ה־callback
    private void handleVersionCheck(boolean isUpdated) {
        if (!isUpdated) {
            Platform.runLater(() -> {
                if (isResponsiveMode())
                {
                    refreshSheet();
                }
                else {
                    updateValueButton.setText("Refresh");  // שינוי טקסט הכפתור
                    updateValueButton.setDisable(false);
                    setUpdatingControlsDisabled(true);
                }
            });
        }
    }


    public void setActive() {
        startVersionCheck(); // התחלת בדיקת גרסה
    }

    public void setInActive() {
        stopVersionCheck(); // עצירת בדיקת גרסה
    }



    // פונקציה שתוכל להשתמש בה כדי לקבל את המצב הנוכחי
    public boolean isResponsiveMode() {
        return isResponsive.get();
    }


    public void resetDynamicAnalysis() {
        String url = RESET_DYNAMIC_ANALYSIS_URL; // כתובת הסרבלט

        // יצירת בקשת POST לסרבלט
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", null)) // בקשת POST ריקה
                .build();

        // קריאה לסרבלט באמצעות HttpClientUtil
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    dynamicAnalysisErrorMassage.setText("Error: " + e.getMessage()); // הצגת הודעת שגיאה במקרה של כשל
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        // ניקוי הטבלה רק אם הבקשה הצליחה
                        sliderTable.getItems().clear();
                        refreshSheet();
                        dynamicMode = false;
                        setActive();
                        if (!readerMode)
                        {
                            cellInputContentTextField.setDisable(false);
                        }
                        if (isResponsiveMode() == false)
                        {
                            updateValueButton.setVisible(true);
                        }
                    } else {
                        // הצגת הודעת כשל בתווית dynamicAnalysisErrorMassage
                        dynamicAnalysisErrorMassage.setText("Failed to reset sliders: " + response.message());
                    }
                });
            }
        });
    }


    public void applyTheme(boolean darkMode) {
        if (sheetViewfinderRootPane != null) {
            sheetViewfinderRootPane.getStylesheets().clear();
            if (darkMode) {
                sheetViewfinderRootPane.getStylesheets().add(getClass().getResource("/component/sheetViewfinder/stylesDarkMode.css").toExternalForm());
            } else {
                sheetViewfinderRootPane.getStylesheets().add(getClass().getResource("/component/sheetViewfinder/styles.css").toExternalForm());
            }
        }
    }



}
