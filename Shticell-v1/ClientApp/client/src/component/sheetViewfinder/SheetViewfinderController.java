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
import javafx.scene.chart.*;
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

    // Main app controller:

    private AppMainController appMainController;



    // Main sheet controller:

    @FXML private SheetController sheetComponentController;
    @FXML private BorderPane sheetViewfinderRootPane;
    private int currentSheetVersion;
    private Timer versionCheckTimer; // updating sheet



    // Previous version functionality:

    @FXML private SheetController sheetVersionController;
    @FXML private ComboBox<Object> versionComboBox;
    @FXML private TabPane mainTabPane;
    @FXML private Tab prevSheetTab;
    @FXML private Tab currentSheetTab;




    // Selection properties:

    private Coordinate selectedCoordinate = null;
    private Coordinate topLeft;
    private Coordinate bottomRight;
    private boolean isProgrammaticChange = false;



    // Modes:

    private boolean readerMode = false;
    private boolean dynamicMode = false;
    @FXML private ToggleButton darkModeToggle;
    @FXML private ToggleButton responsiveToggle;
    // responsive mode boolean property - connected to toggle
    private BooleanProperty isResponsive = new SimpleBooleanProperty(false);





    // Top toolbar:

    @FXML
    private VBox topPane;

    @FXML
    private Label fileNameLabel;

    @FXML
    private Button updateValueButton;

    @FXML
    private Label selectedCellLabel;

    @FXML
    private Label cellUpdateError;

    @FXML
    private Label LastUpdate;

    @FXML
    private Label lastUserUpdatedLabel;







    // Accordion functionality:

    @FXML
    private Accordion accordion;


    // Style:

    // view styling:

    @FXML
    private Slider rowHeightSlider; // slider of row height
    @FXML
    private Slider colWidthSlider; // slider of col width

    // cell styling:

    @FXML
    private ComboBox<Object> alignmentBox;
    @FXML
    private ColorPicker backgroundPicker;
    @FXML
    private ColorPicker fontPicker;



    // Sort:

    @FXML
    private Button sort;

    @FXML
    private Button resetsort;

    private String draggedItem;  // keeping dragged item for drag and drop



    // Filtering:

    @FXML
    private Button resetFiltersButton;

    @FXML
    private ListView<String> colList; // ListView for cols for sorting or filtering



    //Dynamic Analysis:

    private Slider excludedSlider = null; //in movement in dynamic will make sure things dong mess up

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



    // Chart:

    private Coordinate xTopLeft, xBottomRight;

    private Coordinate yTopLeft, yBottomRight;

    @FXML
    private Tab chartTab; // Tab for chart

    @FXML
    private Button selectXButton, clearXButton, selectYButton, clearYButton, generateChart; // Button for generating chart

    @FXML
    private Label selectedx, selectedy; // labels to print selected cells




    // Range and selection:

    // range:

    @FXML
    private TextField rangeNameTextBox;

    @FXML
    private TextField cellInputContentTextField;

    @FXML
    private TabPane columnTabPane;

    @FXML
    private Button addOrDeleteRange;

    @FXML
    private Label rangeErrorMassage;

    @FXML
    private ListView<String> listOfRanges;



    // selection:

    @FXML
    private TextField topLeftBox;

    @FXML
    private TextField bottomRightBox;

    @FXML
    private Button reSelect;

    @FXML
    private Label errorSelectMassage;







    // view finder initialization and general settings:

    @FXML
    private void initialize() {

        // Listener for selection of cell
        sheetComponentController.selectedCellProperty().addListener((observable, oldLabel, newLabel) -> {
            if (newLabel != null) {

                // showing coordinate of selected cell
                selectedCoordinate = sheetComponentController.getSelectedCoordinate();

                // updating text box with value of selected cell
                cellInputContentTextField.setText(sheetComponentController.getSelectedCoordinateOriginalValue());

                selectedCellLabel.setText("Selected cell: " + selectedCoordinate);
                LastUpdate.setText("Last Update: Version " + sheetComponentController.getLastUpdatedVersion());
                lastUserUpdatedLabel.setText("By User: " + sheetComponentController.getLastUserUpdatedCell());

                // **updating sliders of height and width of selected cell properties**
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

                // makes sure the pointer to the end of the line (for continuing keyboard input)
                Platform.runLater(() -> {
                    cellInputContentTextField.requestFocus(); // makes the text box in focus
                    cellInputContentTextField.positionCaret(cellInputContentTextField.getText().length()); // placing it in the end
                    cellInputContentTextField.selectRange(cellInputContentTextField.getText().length(), cellInputContentTextField.getText().length()); // removes selection of text
                });





            } else {
                cellInputContentTextField.setText("");
                selectedCellLabel.setText("Selected cell: none");
            }
        });


        // Listener for range of cells
        sheetComponentController.selectedRangeProperty().addListener((observable, oldRange, newRange) -> {
            if (newRange != null) {
                topLeft = newRange.getTopLeft();
                bottomRight = newRange.getBottomRight();
                selectedCellLabel.setText("Selected range: " + sheetComponentController.actualCellPlacedOnGrid(topLeft) + " to " + sheetComponentController.actualCellPlacedOnGrid(bottomRight));
                topLeftBox.setText(sheetComponentController.actualCellPlacedOnGrid(topLeft).toString());
                bottomRightBox.setText(sheetComponentController.actualCellPlacedOnGrid(bottomRight).toString());

                updateColumnList(sheetComponentController.getSelectedColumns());

                initializeTabsForSelectedColumns(sheetComponentController.getUniqueValuesInRange(topLeft, bottomRight), topLeft, bottomRight);

                // Updating sliders with avg height and width of selected cells
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

                selectedCoordinate = null; // set single selected coordinate as null

            } else {
                selectedCellLabel.setText("No range selected");
            }
        });



        // Listener for "enter" click for sending info of text box for updating cell
        cellInputContentTextField.setOnKeyPressed(event -> {

            if (event.getCode() == KeyCode.ENTER) {
                handleUpdate();
            }

        });



        // listener for ComboBox selection
        versionComboBox.setOnAction(event -> {
            // Getting the index of choice (will match the num of version presented)
            int selectedIndex = versionComboBox.getSelectionModel().getSelectedIndex();

            if (selectedIndex != -1) { // makes sure something is selected
                // load the version selected
                getSheetVersion(selectedIndex+1, sheetDto -> {
                    // taking the dto received and presented dto on the ui
                    sheetVersionController.setPresentedSheet(sheetDto);
                }, errorMessage -> {
                    // error massage in case of error
                    System.out.println("Error: " + errorMessage);
                });


                // switch tabs to version sheet if one is selected
                mainTabPane.getSelectionModel().select(prevSheetTab);
            }

        });




        ObservableList<String> emptyList = FXCollections.observableArrayList();
        colList.setItems(emptyList);  // making ListView empty for selected cols



        // listener for range selected from list of ranges
        listOfRanges.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                sheetComponentController.highlightFunctionRange(newValue); // highlights selection
                rangeNameTextBox.setText(newValue);
                rangeErrorMassage.setText("");
                addOrDeleteRange.setText("Delete Range");
            }
        });

        enableColumnReordering();


        // tabs for selected cols with unique values for filtering
        columnTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        columnTabPane.setSide(Side.TOP); // making sure the tubs stay at the top




        // sliders for height and width
        colWidthSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isProgrammaticChange) { //checks if the user actually moved the slider or placement changed by software, prevents unwanted changes when program changes slider according to new value
                updateColWidth();
            }
        });

        rowHeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isProgrammaticChange) {//checks if the user actually moved the slider or placement changed by software , prevents unwanted changes when program changes slider according to new value
                updateRowHeight();
            }
        });





        // Adding items for alignments
        alignmentBox.getItems().addAll("Left", "Center", "Right");

        // setting default alignment
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

        sheetVersionController.setReadOnly(true); //read only for version controller

        initializeTableColumns();

        initializeStepSizeBox();

        LastUpdate.setText("");

        lastUserUpdatedLabel.setText("");



        // linking toggle to boolean parameter for responsive mode
        responsiveToggle.selectedProperty().bindBidirectional(isResponsive);

        // listener for isResponsive
        isResponsive.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("Responsive mode is ON");
                updateValueButton.setVisible(false);
                // starting responsive mode
            } else {
                System.out.println("Responsive mode is OFF");
                updateValueButton.setVisible(true);
                // closing reponsive mode
            }
        });



        // changes in dark mode toggle
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            appMainController.setDarkMode(newValue); // updated dark mode in AppMainController
        });



        // handles only key input for TextField
        cellInputContentTextField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            // checks if there is a cell selected
            if (selectedCoordinate != null && isResponsiveMode()) { // reacting according to responsive mode for updating - if nor' only after button "update" pressed
                // getting text from text box after any event
                String newValue = cellInputContentTextField.getText();

                try {
                    // params for updating
                    String coordinate = coordinateToString(selectedCoordinate); // translating for coordinate from index selected
                    int currentSheetVersion = sheetComponentController.getCurrentSheetVersion(); // getting current sheet
                    // request for updating sheet
                    sendUpdateRequest(coordinate, newValue, currentSheetVersion);

                } catch (Exception e) {
                    cellUpdateError.setText(e.getMessage());
                }
            }
        });


    }

    public void setAppMainController(AppMainController appMainControll) {
        this.appMainController = appMainControll;

        // setting dark mode toggle according to current mode
        darkModeToggle.setSelected(appMainController.isDarkMode());

        // listener for chances in toggle
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            appMainController.setDarkMode(newValue); // updated darkMode in appMainController
        });
    }

    @FXML
    private void goBackToSheetList() {
        appMainController.switchToAccountArea();  // going back to account area, with auto update of lists
    }

    // getting responsive mode state
    public boolean isResponsiveMode() {
        return isResponsive.get();
    }

    // apply theme according to dark mode state
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

    // set dark mose according to state
    public void setDarkMode(boolean darkMode) {
        darkModeToggle.setSelected(darkMode);
    }





    // Cell value update functions:
    @FXML
    private void handleUpdate() {
        String buttonText = updateValueButton.getText();

        if ("Refresh".equals(buttonText)) { //based on current button function - refresh sheet or update cell
            refreshSheet(); // refresh is requird when something changed since in server last printed version
            if (readerMode)
            {
                updateValueButton.setDisable(true);
            }
            else {
                setUpdatingControlsDisabled(false);
            }
        } else{
            // selected cell
            Coordinate selectedCoordinate = sheetComponentController.getSelectedCoordinate();

            // text from box
            String newValue = cellInputContentTextField.getText().trim();

            // checcks for cell selected
            if (selectedCoordinate != null) {
                try {
                    // params for update req
                    String coordinate = coordinateToString(selectedCoordinate); // translate to cell coordinate
                    int currentSheetVersion = sheetComponentController.getCurrentSheetVersion(); // getting current version

                    // req for updating cell
                    sendUpdateRequest(coordinate, newValue, currentSheetVersion);

                } catch (Exception e) {
                    cellUpdateError.setText(e.getMessage());
                }
            } else {
                // for no cell selected
                cellUpdateError.setText("No cell selected");
            }
        }
    }

    private void sendUpdateRequest(String coordinate, String newValue, int currentSheetVersion) {
        // creating POST request
        RequestBody formBody = new FormBody.Builder()
                .add("coordinate", coordinate)
                .add("newValue", newValue)
                .add("sheetVersion", String.valueOf(currentSheetVersion))
                .build();

        // URL of servlet fot updating of cell
        Request request = new Request.Builder()
                .url(Constants.UPDATE_CELL_URL)
                .post(formBody)
                .build();

        // async request
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
                    // getting the massage in case of error
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                    Platform.runLater(() -> cellUpdateError.setText(errorMessage));
                }
                response.close();
            }
        });
    }





    // Controls disabling:

    // disabling controls that are for sheet updating, when updating not allowed  and needs updating (new version in server)
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

    // Reader mode blocks updating  sheet
    public void setReaderMode(boolean isReaderMode) {
        this.readerMode = isReaderMode;
        disableEditingElements(readerMode);  // blocks according to read mode on or off
    }

    // Disabling editing elements for read mode, according to mode
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

    // in version mode blocks all ui related to sheet
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








    // Version functions:

    // Updating ComboBox of versions
    private void refreshVersionComboBox() {
        versionComboBox.getItems().clear();
        List<Integer> versions = sheetComponentController.getVersionList();

        for (int i = 0; i < versions.size(); i++) {
            int versionNumber = i + 1; // version starts from 1
            int changes = versions.get(i); // num changes for version
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




    // Ranges:

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
        // clearing
        listOfRanges.getItems().clear();

        // adding all objects to list
        listOfRanges.getItems().addAll(sheetComponentController.getRanges().keySet());
    }


    // Deleting range according to name
    public void deleteRange(String rangeName) {
        // creating req body
        RequestBody body = new FormBody.Builder()
                .add("rangeName", rangeName)
                .build();

        // http to server
        Request request = new Request.Builder()
                .url(DELETE_RANGE_URL)
                .post(body)
                .build();

        // async req
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // in case of error the user will be notified on label
                Platform.runLater(() -> {
                    // printing error massage
                    rangeErrorMassage.setText("Failed to delete range: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // checking response from server
                String message;
                if (response.isSuccessful()) {
                    message = "Range '" + rangeName + "' deleted successfully.";
                } else {
                    message = response.body().string();
                }

                Platform.runLater(() -> {
                    // in case of error show massage on label
                    rangeErrorMassage.setText(message);
                });
            }
        });
    }

    // Adding range with a given name and coordinates
    public void addRange(String rangeName, String topLeft, String bottomRight) {
        // creating rew
        RequestBody body = new FormBody.Builder()
                .add("rangeName", rangeName)
                .add("topLeft", topLeft)
                .add("bottomRight", bottomRight)
                .build();

        // http req to server
        Request request = new Request.Builder()
                .url(ADD_RANGE_URL)
                .post(body)
                .build();

        // async req
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // ui notifies error
                Platform.runLater(() -> {
                    // printing error
                    rangeErrorMassage.setText("Failed to add range: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // checking response
                String message;
                if (response.isSuccessful()) {
                    message = "Range '" + rangeName + "' added successfully.";
                } else {
                    message = response.body().string();
                }

                Platform.runLater(() -> {
                    // error from server
                    rangeErrorMassage.setText(message);
                });
            }
        });
    }

    private String coordinateToString(Coordinate coordinate) {
        int column = coordinate.getColumn();
        char columnLetter = (char) ('A' + column - 1);


        int row = coordinate.getRow();

        return String.valueOf(columnLetter) + row;
    }




    // Sorting actions:

    @FXML
    private void handleSortButton() {
        {
            // coordinates from text boxes
            Coordinate topLeft = CoordinateCache.createCoordinateFromString(topLeftBox.getText());
            Coordinate bottomRight = CoordinateCache.createCoordinateFromString(bottomRightBox.getText());

            if (topLeft != null && bottomRight != null) {
                // getting string from ListView
                ObservableList<String> columnStrings = colList.getItems();


                // turning list of string into chars
                List<Character> columnChars = columnStrings.stream()
                        .map(s -> s.charAt(0))  // first char of every string
                        .collect(Collectors.toList());


                // calling sort function in sheet controller
                sheetComponentController.sortRowsInRange(topLeft, bottomRight, columnChars);

                // refreshing sheet with new order
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


    private void enableColumnReordering() {
        colList.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();

            // linking text of cells to data in list
            cell.textProperty().bind(cell.itemProperty());

            // dragging function
            cell.setOnDragDetected(event -> {
                if (cell.getItem() == null) return;

                Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);

                // cancels all selection
                colList.getSelectionModel().clearSelection();

                // Snapshot of text being dragged
                WritableImage snapshot = cell.snapshot(null, null);
                db.setDragView(snapshot);  // making the symbol dragged the col letter

                ClipboardContent cc = new ClipboardContent();
                cc.putString(cell.getItem());
                db.setContent(cc);
                event.consume();
            });

            // getting the dragged item
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);

                    // adding color on other rows hovered on for indicating being replaced
                    cell.setStyle("-fx-background-color: #bc93a3; -fx-border-color: #cc9daa; -fx-border-width: 2px;");
                }
                event.consume();
            });

            // release and set in the correct placement
            cell.setOnDragDropped(event -> {
                if (cell.getItem() == null) return;

                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int draggedIndex = colList.getItems().indexOf(db.getString());
                    int thisIndex = colList.getItems().indexOf(cell.getItem());

                    // adding
                    String draggedItem = colList.getItems().remove(draggedIndex);
                    colList.getItems().add(thisIndex, draggedItem);

                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            // adding og design after adding
            cell.setOnDragExited(event -> {
                if (cell.isSelected()) {
                    // makes style brown in dragging
                    cell.setStyle("-fx-background-color: #8b5e3c; -fx-text-fill: #ffffff;");
                } else {
                    cell.setStyle(""); // back to og style
                }
            });

            // stopping drag after done
            cell.setOnDragDone(event -> {
                colList.getSelectionModel().clearSelection(); // cancels selection
                cell.setStyle(""); // back to og style
                event.consume();
            });

            // selection highlight with color
            cell.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    cell.setStyle("-fx-background-color: #8b5e3c; -fx-text-fill: #ffffff;");
                } else {
                    cell.setStyle(""); // back to og style
                }
            });

            // makes sure style is set when item leaves the row
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







    // Cell style actions:

    @FXML
    public void ChangeBackground(String colorHex) {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();
        // call sheetViewfinder with the style selected
        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "backgroundColor", colorHex);
    }

    @FXML
    public void ChangeTextColor(String colorHex) {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();
        // call sheetViewfinder with the style selected
        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "textColor", colorHex);
    }

    @FXML
    public void ChangeAlignment(String alignment) {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();
        // call sheetViewfinder with the style selected
        sendUpdateCellsStyleRequest(selectedColumns, selectedRows, "alignment", alignment);
    }

    @FXML
    public void resetStyle() {
        List<String> selectedColumns = sheetComponentController.getSelectedColumns();
        List<Integer> selectedRows = sheetComponentController.getSelectedRows();
        // call sheetViewfinder with the style selected
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

        // lists as json to send as params
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
                    response.close();  // closing to avoid leakage
                }
            }
        });
    }

    @FXML
    private void changeBackgroundColor() {

        Color color = backgroundPicker.getValue(); // color
        String colorHex = toHexString(color); // to Hex
        ChangeBackground(colorHex);
    }

    @FXML
    private void changeTextColor() {
        Color color = fontPicker.getValue(); // color
        String colorHex = toHexString(color); // to Hex
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








    // Filter actions:

    @FXML
    private void handleResetFilters() {
        // goes on all tabs of cols
        for (Tab tab : columnTabPane.getTabs()) {
            // getting content of scroll pane in tab
            ScrollPane scrollPane = (ScrollPane) tab.getContent();

            // getting the VBox
            VBox vbox = (VBox) scrollPane.getContent();

            // goes on all checkboxes
            for (Node node : vbox.getChildren()) {
                if (node instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) node;

                    // if nor checked' check it to add item
                    if (!checkBox.isSelected()) {
                        checkBox.setSelected(true);

                        // calling function that returns filtered rows
                        sheetComponentController.addRowsForValue(tab.getText(), checkBox.getText(), topLeft, bottomRight);
                    }
                }
            }
        }

    }

    // building the tabs of cols with lists of values
    public void initializeTabsForSelectedColumns(Map<String, List<String>> columnData, Coordinate topLeft, Coordinate bottomRight) {
        columnTabPane.getTabs().clear(); // cleaning existing

        for (String columnName : columnData.keySet()) {
            // creating new tab for col
            Tab tab = new Tab(columnName);

            // vbox for all checkboxes in col
            VBox vbox = new VBox();

            // adding all unique values with checkbox already checked - creating checkbox
            List<String> uniqueValues = columnData.get(columnName);
            for (String value : uniqueValues) {
                CheckBox checkBox = new CheckBox(value);
                checkBox.setSelected(true); // on default all are checked

                checkBox.setOnAction(event -> {
                    if (checkBox.isSelected()) {
                        // adding the row when checkbox is rechecked
                        sheetComponentController.addRowsForValue(columnName, value, topLeft, bottomRight);
                    } else {
                        sheetComponentController.removeRowsForValue(columnName, value, topLeft, bottomRight);
                    }
                });

                // adding checkbox to item
                vbox.getChildren().add(checkBox);
            }

            // adding vbox to scroll pane to allow scrolling
            ScrollPane scrollPane = new ScrollPane(vbox);
            scrollPane.setFitToWidth(true); // makes sure it fits the width

            // adding scroller to tab
            tab.setContent(scrollPane);

            // adding tab to tab pane
            columnTabPane.getTabs().add(tab);
        }
    }






    // Rows and Cols actions:

    @FXML
    private void updateColWidth() {
        double newWidth = colWidthSlider.getValue(); // value from slider


        sheetComponentController.updateColWidth(newWidth);

    }

    @FXML
    private void updateRowHeight() {
        double newHeight = rowHeightSlider.getValue(); // value from slider

        sheetComponentController.updateRowHeight(newHeight);

    }








    // Dynamic Analysis

    private void initializeTableColumns() {
        // name of cells
        cellNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        // sliders
        sliderCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getValue()));

        // list of items for table
        sliderTable.setItems(sliderData);
    }

    @FXML
    private void addSliderForCell() {
        // reading values from text field
        String fromText = sliderFromTextfield.getText();
        String toText = sliderToTextfield.getText();

        // checks if values valid
        if (!isNumeric(fromText) || !isNumeric(toText)) {
            dynamicAnalysisErrorMassage.setText("Please enter numeric values only.");
            return;
        }

        double fromValue = Double.parseDouble(fromText);
        double toValue = Double.parseDouble(toText);
        double stepSize = stepSizeChoice.getSelectionModel().getSelectedItem();

        // checking range and -step size
        if (!isValidRange(fromValue, toValue, stepSize)) {
            dynamicAnalysisErrorMassage.setText("Range is not large enough for the selected step size.");
            return;
        }

        // removing error massage
        dynamicAnalysisErrorMassage.setText("");

        // saving coordinate selected in local value
        String cellCoordinate = coordinateToString(selectedCoordinate);

        // new slide
        Slider newSlider = new Slider(fromValue, toValue, (fromValue + toValue) / 2);

        //  stepSize of slider
        newSlider.setBlockIncrement(stepSize);
        newSlider.setMajorTickUnit(stepSize);
        newSlider.setMinorTickCount(0);
        newSlider.setSnapToTicks(true);

        // checks if user is moving slider (to prevent changes when program adjusts position)
        final boolean[] isUserAction = {false};

        // listeners for interacting with sliders
        newSlider.setOnMousePressed(event -> {
            isUserAction[0] = true;
            dynamicMode = true;
            updateValueButton.setVisible(false);
            setInActive(); // stopping automatic updating so it doesnt interfere with dynamic mode
            cellInputContentTextField.setDisable(true);
            excludedSlider = newSlider; // exculuding slider for autumatic adjustment when user interacting with it - prevents Jittery
        });

        newSlider.setOnMouseReleased(event -> {
            isUserAction[0] = false;
            excludedSlider = null; // making excludedSlider null when user finished interacting with slider excluded
        });


        // saves last value to make sure only update in real case of change
        final double[] lastSentValue = {newSlider.getValue()};

        // changes when user leaves slider
        newSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isUserAction[0]) { // only on direct click will send new req for updating
                double roundedValue = Math.round(newVal.doubleValue() / stepSize) * stepSize;

                if (roundedValue != lastSentValue[0]) {
                    lastSentValue[0] = roundedValue; // updating the most recent value sent
                    String newValueStr = Double.toString(roundedValue);

                    updateCellInTemporarySheet(cellCoordinate, newValueStr, sheetDto -> {
                        sheetComponentController.setPresentedSheet(sheetDto);
                        updateSlidersPosition(); // adjusting all slider except excluded one the user interacting with
                        System.out.println("Updated sheet DTO to version: " + sheetDto.getVersion());

                        // updating all other lists in UI for any change that can be caused just in case
                        updateListOfRanges();
                        refreshVersionComboBox();
                    }, errorMessage -> {
                        System.out.println("Error updating cell: " + errorMessage);
                    });
                }
            }
        });

        // initialized value for point in slide according to cell
        String value = sheetComponentController.getCellValue(cellCoordinate);

        if (isNumeric(value)) {
            double cellValue = Double.parseDouble(value);
            if (cellValue >= fromValue && cellValue <= toValue) { // if in range position point accordingly
                newSlider.setValue(cellValue);
            } else if (cellValue < fromValue) { // bigger than range set to the right
                newSlider.setValue(fromValue);
            } else {
                newSlider.setValue(toValue); // smaller than range set to the left
            }
        } else {
            newSlider.setValue((fromValue + toValue) / 2); // with no numeric value set it to the middle
        }

        // adding name of cell and its slider
        sliderData.add(new AbstractMap.SimpleEntry<>(cellCoordinate, newSlider));
    }

    // Updates all sliders except the one user is interacting with
    private void updateSlidersPosition() {
        for (Map.Entry<String, Slider> entry : sliderData) {
            Slider slider = entry.getValue();
            if (slider != excludedSlider) {  // if the slide is not the one user is moving
                String cellCoordinate = entry.getKey();
                String cellValue = sheetComponentController.getCellValue(cellCoordinate);

                if (isNumeric(cellValue)) {
                    double cellValueDouble = Double.parseDouble(cellValue);
                    if (cellValueDouble >= slider.getMin() && cellValueDouble <= slider.getMax()) { // if in range position point accordingly
                        slider.setValue(cellValueDouble);
                    } else if (cellValueDouble < slider.getMin()) {// bigger than range set to the right
                        slider.setValue(slider.getMin());
                    } else {
                        slider.setValue(slider.getMax()); // smaller than range set to the left
                    }
                } else {
                    slider.setValue((slider.getMin() + slider.getMax()) / 2);// with no numeric value set it to the middle
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
        stepSizeChoice.getItems().addAll(100.0, 10.0, 1.0, 0.1, 0.01, 0.001, 0.5, 0.05, 0.005);

        stepSizeChoice.setValue(1.0); // default is 1
    }

    // Async post req for cell updating in temp sheet - returns updated sheet dto
    private void updateCellInTemporarySheet(String cellId, String newValue, Consumer<SheetDto> onSuccess, Consumer<String> onError) {
        // creating req
        HttpUrl.Builder urlBuilder = HttpUrl.parse(UPDATE_TEMP_SHEET_URL).newBuilder();
        String finalUrl = urlBuilder.build().toString();

        // params : cellId newValue
        RequestBody formBody = new FormBody.Builder()
                .add("cellId", cellId)
                .add("newValue", newValue)
                .build();

        // creating url for post req
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(formBody)
                .build();

        // sending async req
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // in error situation - print error
                Platform.runLater(() -> onError.accept("Error updating cell: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // using JSONUtils to convert response to dto
                    String responseBody = response.body().string();
                    SheetDto sheetDto = JSONUtils.fromJson(responseBody, SheetDtoImpl.class);

                    // when req success - UI Thread
                    Platform.runLater(() ->
                    {
                        onSuccess.accept(sheetDto);
                        sheetComponentController.setPresentedSheet(sheetDto);
                        updateSlidersPosition();
                    });
                } else {
                    // if failed
                    String errorMessage = "Failed to update cell. Response code: " + response.code();
                    Platform.runLater(() -> onError.accept(errorMessage));
                }
            }
        });
    }

    // resets temp sheet of dynamic analysis
    public void resetDynamicAnalysis() {
        String url = RESET_DYNAMIC_ANALYSIS_URL; // servlet address

        // creating post req
        //
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create("", null)) // empty post req
                .build();

        //  calling servlet
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    dynamicAnalysisErrorMassage.setText("Error: " + e.getMessage()); // display error in case of failure
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        // cleaning table if req succeed of dynamic sliders
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
                        // display error in dynamicAnalysisErrorMassage in case reset was not successful
                        dynamicAnalysisErrorMassage.setText("Failed to reset sliders: " + response.message());
                    }
                });
            }
        });
    }





    // Chart Generation:


    // Selection of X axis
    @FXML
    private void selectXAction(ActionEvent event) {
        // משתמש ב-topLeft ו-bottomRight שהוזנו ע"י ליסטנר ה-range
        if (topLeft != null && bottomRight != null && topLeft.getColumn() == bottomRight.getColumn()) {
            xTopLeft = topLeft;
            xBottomRight = bottomRight;
            String from = sheetComponentController.actualCellPlacedOnGrid(xTopLeft).toString();
            String to   = sheetComponentController.actualCellPlacedOnGrid(xBottomRight).toString();
            selectedx.setText(from + " to " + to);
        }
    }

    // Clearing X axis selection
    @FXML
    private void clearXAction(ActionEvent event) {
        xTopLeft = xBottomRight = null;
        selectedx.setText("");
    }

    // Selection of Y axis
    @FXML
    private void selectYAction(ActionEvent event) {
        if (topLeft != null && bottomRight != null && topLeft.getColumn() == bottomRight.getColumn()) {
            yTopLeft = topLeft;
            yBottomRight = bottomRight;
            String from = sheetComponentController.actualCellPlacedOnGrid(yTopLeft).toString();
            String to   = sheetComponentController.actualCellPlacedOnGrid(yBottomRight).toString();
            selectedy.setText(from + " to " + to);
        }
    }

    // Clearing Y axis selection
    @FXML
    private void clearYAction(ActionEvent event) {
        yTopLeft = yBottomRight = null;
        selectedy.setText("");
    }

    // Creating the graph in the tab
    @FXML
    private void generateChartAction(ActionEvent event) {
        // if one is nor selected will not perform
        if (xTopLeft == null || yTopLeft == null) return;

        // line chart
        boolean isBarChart = false;
        Node chartNode = buildChart(isBarChart);

        // putting chart in tab
        BorderPane pane = new BorderPane(chartNode);
        pane.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        chartTab.setContent(pane);
    }

    // creating the chart from the library, from Chat GPT
    private Node buildChart(boolean isBarChart) {
        if (isBarChart) {
            // connecting data from x to y
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Data");


            // getting the data from the selected cells
            for (int r = xTopLeft.getRow(); r <= xBottomRight.getRow(); r++) {
                // building string for x
                char colLetterX = (char)('A' + xTopLeft.getColumn() - 1);
                String cellRefX = colLetterX + String.valueOf(r);
                String xCat = sheetComponentController.getCellValue(cellRefX);

                //for y
                char colLetterY = (char)('A' + yTopLeft.getColumn() - 1);
                String cellRefY = colLetterY + String.valueOf(r);
                String yStr = sheetComponentController.getCellValue(cellRefY);

                // parsing to line chart
                Number yVal = isNumeric(yStr) ? Double.parseDouble(yStr) : 0;
                series.getData().add(new XYChart.Data<>(xCat, yVal));
            }

            chart.getData().add(series);
            return chart;

        } else {
            // if there are numbers on two axis
            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data");

            for (int r = xTopLeft.getRow(); r <= xBottomRight.getRow(); r++) {
                //string for x
                char colLetterX = (char)('A' + xTopLeft.getColumn() - 1);
                String cellRefX = colLetterX + String.valueOf(r);
                String xStr     = sheetComponentController.getCellValue(cellRefX);

                // string for y
                char colLetterY = (char)('A' + yTopLeft.getColumn() - 1);
                String cellRefY = colLetterY + String.valueOf(r);
                String yStr     = sheetComponentController.getCellValue(cellRefY);

                // adds a dot is both are numbers
                if (isNumeric(xStr) && isNumeric(yStr)) {
                    double xv = Double.parseDouble(xStr);
                    double yv = Double.parseDouble(yStr);
                    series.getData().add(new XYChart.Data<>(xv, yv));
                }
            }

            chart.getData().add(series);
            return chart;
        }
    }






    // Updating and managing sheet:


    // refreshing displayed sheet
    private void refreshSheetDisplay() {
        sheetComponentController.loadSheetCurrent();
    }


    // set the sheet user controls - calls setCurrentSheet for server call
    public void setSheet(String sheetName) {
        System.out.println("Displaying sheet: " + sheetName);
        fileNameLabel.setText(sheetName);

        try {
            setCurrentSheet(sheetName, log -> System.out.println(log)); // sending req for updating
        } catch (IOException e) {
            e.printStackTrace();
        }

        // async to get the updated SheetDto
        getCurrentSheet(sheetDto -> {
            if (sheetDto != null) {
                System.out.println("Sheet loaded successfully: " + sheetName);
                sheetComponentController.setPresentedSheet(sheetDto);
                updateListOfRanges();
                refreshVersionComboBox();
                currentSheetVersion = sheetComponentController.getCurrentSheetVersion();

                // starting to check version updated only after SheetDto is updated (to prevent locking ui elements for vain)
                setActive();
            }
        }, errorMessage -> {
            System.out.println("Error: " + errorMessage);
        });
    }

    // post req to set the current sheet according to sheet name
    public static void setCurrentSheet(String sheetName, Consumer<String> httpRequestLogger) throws IOException {
        // creating req
        RequestBody formBody = new FormBody.Builder()
                .add("sheetName", sheetName)  // adding param SheetName
                .build();

        Request request = new Request.Builder()
                .url(SET_SHEET_URL)
                .post(formBody)
                .build();

        // sending async req to not block UI
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                httpRequestLogger.accept("Error setting current sheet: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // using try-with-resources so req will close automatically
                try (response) {
                    if (response.isSuccessful()) {
                        httpRequestLogger.accept("Successfully set current sheet to: " + sheetName);
                    } else {
                        httpRequestLogger.accept("Failed to set current sheet. Response code: " + response.code());
                    }
                }
            }
        });
    }

    // get the current sheet DTO with async get req
    public void getCurrentSheet(Consumer<SheetDto> onSuccess, Consumer<String> onError) {
        String finalUrl = HttpUrl
                .parse(String.valueOf(URI.create(Constants.SHEET_URL)))
                .toString();


        // creating get req
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // send the rew async
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // in case of failure show massage to user
                Platform.runLater(() -> onError.accept("Error fetching sheet: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // converts response to sheetDTO using JSONUtils
                    String responseBody = response.body().string();
                    SheetDto sheetDto = JSONUtils.fromJson(responseBody, SheetDtoImpl.class);

                    //  on success calling UI Thread accept func and saving sheet
                    Platform.runLater(() -> onSuccess.accept(sheetDto));
                } else {
                    // in case of failure show error massage
                    String errorMessage = "Failed to fetch current sheet. Response code: " + response.code();
                    Platform.runLater(() -> onError.accept(errorMessage));
                }
            }
        });
    }


    // returns current version of sheet in server
    public void getSheetVersion(int version, Consumer<SheetDto> onSuccess, Consumer<String> onError) {
        String finalUrl = HttpUrl
                .parse(String.valueOf(URI.create(Constants.GET_SHEET_VERSION_URL)))
                .newBuilder()
                .addQueryParameter("version", String.valueOf(version)) // adding version number as parameter
                .toString();


        // creating get req
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        // sending async req
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // in case of failure show massage
                Platform.runLater(() -> onError.accept("Error fetching sheet version: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    // converting json to sheet dto
                    String responseBody = response.body().string();
                    SheetDto sheetDto = JSONUtils.fromJson(responseBody, SheetDtoImpl.class);

                    // setting the sheet version
                    Platform.runLater(() -> onSuccess.accept(sheetDto));
                } else {
                    // display error massage
                    String errorMessage = "Failed to fetch sheet version. Response code: " + response.code();
                    Platform.runLater(() -> onError.accept(errorMessage));
                }
            }
        });
    }

    // returns current version of sheet displayed
    private int getCurrentLocalSheetVersion() {
        return sheetComponentController.getCurrentSheetVersion(); // getting current version displayed in sheet element
    }

    // starts automatic check to keep sheet updated to new version
    public void startVersionCheck() {
        versionCheckTimer = new Timer(true);
        versionCheckTimer.schedule(new SheetVersionRefresher(this::getCurrentLocalSheetVersion, this::handleVersionCheck), Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    // stopping automatic check of version of sheet
    public void stopVersionCheck() {
        if (versionCheckTimer != null) {
            versionCheckTimer.cancel();
            versionCheckTimer = null;
        }
    }

    public void setActive() {
        startVersionCheck(); // starting version check
    }

    public void setInActive() {
        stopVersionCheck(); // stopping version check
    }

    // handle answer of if version not up to date to perform operations needed in case of not up to date
    private void handleVersionCheck(boolean isUpdated) {
        if (!isUpdated) {
            Platform.runLater(() -> {
                if (isResponsiveMode())
                {
                    refreshSheet();
                }
                else {
                    updateValueButton.setText("Refresh");  // changing text of button to refresh if sheet not updated
                    updateValueButton.setDisable(false);
                    setUpdatingControlsDisabled(true);
                }
            });
        }
    }

    private void refreshSheet() {
        // calling the sheet dto req from server
        getCurrentSheet(sheetDto -> {
            sheetComponentController.setPresentedSheet(sheetDto);

            updateListOfRanges(); // set new list
            refreshVersionComboBox(); // set new version list
            updateSlidersPosition(); // set sliders position reset

            currentSheetVersion = sheetComponentController.getCurrentSheetVersion();
            // changing button to "Update" text again after the sheet updated to new version
            updateValueButton.setText("Update");

            System.out.println("Updated sheet to version: " + sheetDto.getVersion());

        }, errorMessage -> {
            System.out.println("Error refreshing sheet: " + errorMessage);
        });
    }


}
