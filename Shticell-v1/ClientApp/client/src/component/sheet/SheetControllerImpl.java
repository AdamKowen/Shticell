package component.sheet;

import component.cellrange.CellRange;
import dto.BoundariesDto;
import dto.CellDto;
import dto.RangeDto;
import dto.SheetDto;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class SheetControllerImpl implements SheetController {


    // Models for connecting Sheet to UI
    SheetDto DisplayedSheet = null;
    private UImodel uiModel;
    private final Map<Coordinate, Pane> overlayMap = new HashMap<>();

    // sheet UI objects
    @FXML
    public ScrollPane sheetScrollPane;
    @FXML
    private GridPane sheetGridPane;


    // Grid adjustments
    private final double cellWidth = 100.0; // default width for cells
    private final double cellHeight = 40.0; // default height for cells
    private Map<String, Double> columnWidth = new HashMap<>(); // map for storing width of every col
    private Map<Integer, Double> rowHeight = new HashMap<>(); // map for storing height of every row


    // printing order managing:
    private List<Integer> sortedRowOrder; // the main printing list. order and which rows to print
    private List<Integer> lastSortedOrderBeforeFiltering; // full order of rows before filtering
    Map<Integer, Integer> rowIndexMap; // map from og row index to place of printed row
    private Map<Integer, Integer> removalCountMap = new HashMap<>(); // counts num of objects causing row to be filtered


    // Cell/Coordinate/Range selection management
    private Coordinate selectedCoordinate; // saves the selected coordinate
    private ObjectProperty<Label> selectedCell; // saves the selected cell (the cell presented in coordinate)
    private Coordinate startCoordinate; // coordinate that selection was started from
    private Coordinate endCoordinate; // coordinate that selection was released on
    private ObjectProperty<CellRange> selectedRange = new SimpleObjectProperty<>(); //object to keep range selected
    private boolean isDragging = false; // checks if mouse is in drag action


    // Animations for click and drag (from chatGPT)
    private AnimationTimer scrollTimer;
    private static final double SCROLL_SPEED = 0.01; // scrolling speed
    private static final double EDGE_THRESHOLD = 20; // distant from edge of ScrollPane that the scrolling starts
    private double scrollDirectionX = 0;
    private double scrollDirectionY = 0;


    // Access handling
    private boolean isReadOnly = false;


    @FXML
    private void initialize() {
        // creating UI model for all cells
        uiModel = new UImodel();

        // Listener for selected cell
        selectedCell = new SimpleObjectProperty<>();
        selectedCell.addListener((observableValue, oldLabelSelection, newSelectedLabel) -> {
            if (oldLabelSelection != null) {
                oldLabelSelection.setId(null); // cancelling previous selection
            }
            if (newSelectedLabel != null) {
                newSelectedLabel.setId("selected-cell");

                if(!isReadOnly)
                {
                    selectedCoordinate = getCoordinateForLabel(newSelectedLabel);  // updating coordinate with the og cell
                }
            }
        });


        // listeners for mouse events on Scroll Pane
        sheetScrollPane.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMove);
        sheetScrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseMove);


        // scroller animation - creating AnimationTimer
        scrollTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (scrollDirectionX != 0 || scrollDirectionY != 0) {
                    double hValue = sheetScrollPane.getHvalue() + scrollDirectionX * SCROLL_SPEED;
                    double vValue = sheetScrollPane.getVvalue() + scrollDirectionY * SCROLL_SPEED;

                    // values between 0 to 1
                    hValue = Math.max(0, Math.min(hValue, 1));
                    vValue = Math.max(0, Math.min(vValue, 1));

                    sheetScrollPane.setHvalue(hValue);
                    sheetScrollPane.setVvalue(vValue);
                }
            }
        };


    }




    // scrolling animation:

    // reaction to mouse movement and position
    private void handleMouseMove(MouseEvent event) {

        Bounds viewportBounds = sheetScrollPane.getViewportBounds(); // Get the bounds (visible area) of the ScrollPane
        Bounds contentBounds = sheetScrollPane.getContent().getLayoutBounds();// Get the full bounds of the content inside the ScrollPane


        double mouseX = event.getX(); // Get the X coordinate of the mouse relative to the ScrollPane
        double mouseY = event.getY(); // Get the Y coordinate of the mouse relative to the ScrollPane


        double viewportWidth = viewportBounds.getWidth(); // Width of the visible area in the ScrollPane
        double viewportHeight = viewportBounds.getHeight(); // Height of the visible area in the ScrollPane


        // Check if the mouse is near the left edge and dragging
        if (mouseX <= EDGE_THRESHOLD && isDragging) {
            scrollDirectionX = -1; // Scroll left
        }
        // Check if the mouse is near the right edge and dragging
        else if (mouseX >= viewportWidth - EDGE_THRESHOLD && isDragging) {
            scrollDirectionX = 1; // Scroll right
        }
        // Otherwise, do not scroll horizontally
        else {
            scrollDirectionX = 0;
        }


        // Check if the mouse is near the top edge and dragging
        if (mouseY <= EDGE_THRESHOLD && isDragging) {
            scrollDirectionY = -1; // Scroll up
        }
        // Check if the mouse is near the bottom edge and dragging
        else if (mouseY >= viewportHeight - EDGE_THRESHOLD && isDragging) {
            scrollDirectionY = 1; // Scroll down
        }
        // Otherwise, do not scroll vertically
        else {
            scrollDirectionY = 0;
        }



        // Start the scrolling animation if any direction is active
        if (scrollDirectionX != 0 || scrollDirectionY != 0) {
            scrollTimer.start(); // Start autoscrolling
        } else {
            scrollTimer.stop(); // Stop autoscrolling
        }



        // If the mouse is being dragged with the primary button pressed
        if (event.isPrimaryButtonDown()) {
            // Get the current mouse position in scene coordinates
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY()); // Get the current mouse position in scene coordinates

            // Convert scene coordinates to local coordinates in the GridPane
            Point2D mouseGridCoords = sheetGridPane.sceneToLocal(mouseSceneCoords);

            // Determine the column index based on the X position
            int col = getColumnIndexAtX(mouseGridCoords.getX());

            // Determine the row index based on the Y position
            int row = getRowIndexAtY(mouseGridCoords.getY());

            // Ignore header cells (row 0 and column 0)
            if (col >= 1 && row >= 1) {
                // Create a Coordinate object for the current cell
                Coordinate currentCoordinate = CoordinateCache.createCoordinate(row, col);
                // If the new coordinate is different from the current end of selection
                if (!currentCoordinate.equals(endCoordinate)) {
                    endCoordinate = currentCoordinate;
                    // Update the highlighted selection range on the UI thread
                    Platform.runLater(() -> highlightSelectedRange(startCoordinate, endCoordinate));
                    // Set the selected range in the controller/model
                    setSelectedRange(startCoordinate, endCoordinate);
                }
            }
        }
    }

    // Returns the column index in the GridPane that corresponds to the given X coordinate (in pixels)
    private int getColumnIndexAtX(double x) {
        double accumulatedWidth = 0;
        for (int i = 0; i < sheetGridPane.getColumnConstraints().size(); i++) {
            accumulatedWidth += sheetGridPane.getColumnConstraints().get(i).getPrefWidth(); // adds width of every col
            if (x < accumulatedWidth) { //checks if value of x in the range calculated
                return i;
            }
        }
        return -1;
    }

    // Returns the row index in the GridPane that corresponds to the given Y coordinate (in pixels)
    private int getRowIndexAtY(double y) {
        double accumulatedHeight = 0;
        for (int i = 0; i < sheetGridPane.getRowConstraints().size(); i++) {
            accumulatedHeight += sheetGridPane.getRowConstraints().get(i).getPrefHeight(); // adds height of every col
            if (y < accumulatedHeight) { //checks if value of y in the range calculated
                return i;
            }
        }
        return -1;
    }





    // Mouse Events for select, drag and release:

    private void addMouseEvents(StackPane cellPane, Coordinate coordinate) {

        // Start of mouse click event
        cellPane.setOnMousePressed(event -> {
            startCoordinate = coordinate; // saving beggining
            endCoordinate = coordinate;
            if(!isReadOnly)
            {
                selectedCell.set(getLabelByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
            }
            highlightSelectedRange(startCoordinate, endCoordinate);
            System.out.println("Mouse pressed at: " + startCoordinate.getRow() + ", " + startCoordinate.getColumn());
        });

        // detection drag - starting drag event
        cellPane.setOnDragDetected(event -> {
            cellPane.startFullDrag(); // starting full drag
            isDragging = true;
            System.out.println("Drag detected, starting full drag.");
        });

        // event of hovering while full drag
        cellPane.setOnMouseDragOver(event -> {
            if (coordinate != endCoordinate) {
                System.out.println("Mouse dragged over: " + coordinate.getRow() + ", " + coordinate.getColumn());
                endCoordinate = coordinate;
                highlightSelectedRange(startCoordinate, endCoordinate); // updating selection as drag goes on
                if (startCoordinate != endCoordinate) {
                    selectedCell.set(null);
                    setSelectedRange(startCoordinate, endCoordinate);
                } else {

                    if(!isReadOnly)
                    {
                        selectedCell.set(getLabelByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
                    }
                }
            }
        });

        // release click event
        cellPane.setOnMouseReleased(event -> {
            System.out.println("Mouse released at: " + endCoordinate.getRow() + ", " + endCoordinate.getColumn());
            isDragging = false;
            highlightSelectedRange(startCoordinate, endCoordinate); // re marking selection
            if (startCoordinate == endCoordinate) {
                if(!isReadOnly)
                {
                    selectedCell.set(getLabelByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
                }
                selectedCoordinate = startCoordinate;
            } else {
                selectedCell.set(null);
                selectedCoordinate = null;
                setSelectedRange(startCoordinate, endCoordinate);
            }
        });
    }

    private void addMouseEventsToColumnHeader(Label columnHeader, int colIndex) {
        columnHeader.setOnMousePressed(event -> { //select all col and highlighting selection
            isDragging = true;
            startCoordinate = CoordinateCache.createCoordinate(1, colIndex);
            endCoordinate = CoordinateCache.createCoordinate(sheetGridPane.getRowConstraints().size() - 1, colIndex);
            highlightSelectedRange(startCoordinate, endCoordinate);
        });

        columnHeader.setOnMouseDragged(event -> { //with drag - select all cols in range of point2d
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
            Point2D mouseGridCoords = sheetGridPane.sceneToLocal(mouseSceneCoords);
            int currentCol = getColumnIndexAtX(mouseGridCoords.getX());

            if (currentCol >= 1 && currentCol <= sheetGridPane.getColumnConstraints().size()) { //finds all colls
                int minCol = Math.min(colIndex, currentCol);
                int maxCol = Math.max(colIndex, currentCol);

                startCoordinate = CoordinateCache.createCoordinate(1, minCol);
                endCoordinate = CoordinateCache.createCoordinate(sheetGridPane.getRowConstraints().size() - 1, maxCol);
                highlightSelectedRange(startCoordinate, endCoordinate);
            }
        });

        columnHeader.setOnMouseReleased(event -> {
            isDragging = false; //stops dragging
            setSelectedRange(startCoordinate, endCoordinate);
        });
    }

    private void addMouseEventsToRowHeader(Label rowHeader, int rowIndex) {
        rowHeader.setOnMousePressed(event -> { //starts dragging on rows
            isDragging = true;
            startCoordinate = CoordinateCache.createCoordinate(rowIndex, 1);
            endCoordinate = CoordinateCache.createCoordinate(rowIndex, sheetGridPane.getColumnConstraints().size() - 1);
            highlightSelectedRange(startCoordinate, endCoordinate);
        });

        rowHeader.setOnMouseDragged(event -> { // using points for finding rows in range
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
            Point2D mouseGridCoords = sheetGridPane.sceneToLocal(mouseSceneCoords);
            int currentRow = getRowIndexAtY(mouseGridCoords.getY());

            if (currentRow >= 1 && currentRow <= sheetGridPane.getRowConstraints().size()) {
                int minRow = Math.min(rowIndex, currentRow);
                int maxRow = Math.max(rowIndex, currentRow);

                startCoordinate = CoordinateCache.createCoordinate(minRow, 1);
                endCoordinate = CoordinateCache.createCoordinate(maxRow, sheetGridPane.getColumnConstraints().size() - 1);
                highlightSelectedRange(startCoordinate, endCoordinate);
            }
        });

        // mouse release - stop drag
        rowHeader.setOnMouseReleased(event -> {
            isDragging = false;
            setSelectedRange(startCoordinate, endCoordinate);
        });
    }





    // Refreshing all data in sheet:
    @Override
    public void updateSheet() {
        String backgroundColor;
        String fontColor;
        String alignment;

        // cleaning grid pane
        sheetGridPane.getChildren().clear();
        sheetGridPane.setGridLinesVisible(false); // set grid lines not visible
        sheetGridPane.setHgap(0);
        sheetGridPane.setVgap(0);

        startCoordinate = CoordinateCache.createCoordinate(1,1); //start coordinate for first dragging selecting if occurrs

        // clearing size
        sheetGridPane.getRowConstraints().clear();
        sheetGridPane.getColumnConstraints().clear();


        // brining sorted order for printing if there isn't one
        if (sortedRowOrder == null) {
            sortedRowOrder = DisplayedSheet.resetSoretedOrder();
        }

        if (lastSortedOrderBeforeFiltering == null) {
            lastSortedOrderBeforeFiltering = new ArrayList<>();
            lastSortedOrderBeforeFiltering.addAll(sortedRowOrder);
        }

        // col headers
        for (int col = 0; col < DisplayedSheet.getNumOfColumns(); col++) {
            char columnLetter = (char) ('A' + col); // A, B, C וכו'
            Label columnHeader = new Label(String.valueOf(columnLetter));
            columnHeader.getStyleClass().add("sheet-header-text");
            columnHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // setting background for header
            sheetGridPane.add(columnHeader, col + 1, 0);
            columnHeader.getStyleClass().add("sheet-header-background");

            // mouse actions for selection col on click
            addMouseEventsToColumnHeader(columnHeader, col + 1);

        }


        // row headers
        // according to printed order
        for (int rowIndex = 0; rowIndex < sortedRowOrder.size(); rowIndex++) {
            int actualRow = sortedRowOrder.get(rowIndex);  // according to printing order
            Label rowHeader = new Label(String.valueOf(actualRow));
            rowHeader.getStyleClass().add("sheet-header-text");
            rowHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // background color
            sheetGridPane.add(rowHeader, 0, rowIndex + 1); // row 0 header - the rest is value + 1
            rowHeader.getStyleClass().add("sheet-header-background");

            // mouse events for selection of row
            addMouseEventsToRowHeader(rowHeader, rowIndex + 1);
        }



        // size of rows
        // (row == 0)
        RowConstraints headerRowConstraints = new RowConstraints();
        headerRowConstraints.setPrefHeight(cellHeight);
        headerRowConstraints.setMinHeight(cellHeight);
        headerRowConstraints.setMaxHeight(cellHeight);
        sheetGridPane.getRowConstraints().add(headerRowConstraints);

        // for printed rows
        for (int i = 0; i < sortedRowOrder.size(); i++) {
            RowConstraints rowConstraints = new RowConstraints();

            int rowNumber = sortedRowOrder.get(i);

            // according to value of height, or by default cellHeight
            double height = rowHeight.getOrDefault(rowNumber, cellHeight);

            rowConstraints.setPrefHeight(height);
            rowConstraints.setMinHeight(height);
            rowConstraints.setMaxHeight(height);

            sheetGridPane.getRowConstraints().add(rowConstraints);
        }



        // size printed col

        // (row == 0)
        ColumnConstraints headerColConstraints = new ColumnConstraints();
        headerColConstraints.setPrefWidth(cellWidth);
        headerColConstraints.setPrefWidth(cellWidth);
        headerColConstraints.setPrefWidth(cellWidth);
        sheetGridPane.getColumnConstraints().add(headerColConstraints);


        // for printed cols
        for (int col = 1; col <= DisplayedSheet.getNumOfColumns(); col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();

            // name of col
            String colKey = convertColumnNumberToString(col);

            // cell width or default
            double width = columnWidth.getOrDefault(colKey, cellWidth);

            colConstraints.setPrefWidth(width);
            colConstraints.setMinWidth(width);
            colConstraints.setMaxWidth(width);

            sheetGridPane.getColumnConstraints().add(colConstraints);
        }


        // adding cells according to printed order
        for (int rowIndex = 0; rowIndex < sortedRowOrder.size(); rowIndex++) {
            int actualRow = sortedRowOrder.get(rowIndex);  // according to printed order
            for (int col = 1; col <= DisplayedSheet.getNumOfColumns(); col++) {

                Coordinate coordinate = CoordinateCache.createCoordinate(rowIndex + 1, col); // coordinate according to gridpane
                CellDto cell = DisplayedSheet.getCell(actualRow, col);


                // creating stackpane for cell
                StackPane cellPane = new StackPane();
                cellPane.setPadding(Insets.EMPTY); // no padding for stack

                // new label
                Label label = new Label();

                // linking label to StringProperty from UImodel
                String cellValue = (cell != null && cell.getValue() != null && !cell.getValue().isEmpty()) ? cell.getValue() : "";
                uiModel.updateCell(coordinate, cellValue);  // updating cell in UImodel
                label.textProperty().bind(uiModel.getCellProperty(coordinate)); // Binding

                // cutting text according to max width
                label.setMaxWidth(cellWidth);
                label.setWrapText(false); // no wrap
                label.setEllipsisString("..."); // three dots when text get cut

                // label transparent
                label.setBackground(Background.EMPTY);




                // Panes for selection of cells:

                Pane selectionOverlay = new Pane();
                selectionOverlay.setPadding(Insets.EMPTY);

                selectionOverlay.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                selectionOverlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                selectionOverlay.setPrefWidth(Double.MAX_VALUE);
                selectionOverlay.setPrefHeight(Double.MAX_VALUE);


                selectionOverlay.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                selectionOverlay.setMouseTransparent(true); // wont interrupt mouse events



                // saves a map of panes, that we can update for selection
                overlayMap.put(coordinate, selectionOverlay);
                // adding Pane to the cell placement (above label)
                cellPane.getChildren().add(selectionOverlay);
                // label to cell pane
                cellPane.getChildren().add(label);


                if (cell != null) //if cell has value
                {
                    // getting the cell color from curr cell
                    backgroundColor = cell.getStyle().getBackgroundColor(); // returns color string

                    // getting the font color from curr cell
                    fontColor = cell.getStyle().getTextColor(); // returns font color string


                    alignment = cell.getStyle().getAlignment();


                    if (Objects.equals(backgroundColor, "#FFFFFF"))
                    {
                        // setting cell pane with default if it was the system default (white) for dark mode support
                        cellPane.getStyleClass().add("cellDefault");

                    }
                    else
                    {
                        // setting the color if it is not default color
                        cellPane.setStyle("-fx-background-color: " + backgroundColor + "; -fx-padding: 5px;");

                    }


                    if (Objects.equals(fontColor, "#000000"))
                    {
                        // font color set to default (with support for dark mode) according to system default
                        label.getStyleClass().add("labelDefault");
                    }
                    else
                    {
                        // if not default color will shoe the font color
                        label.setStyle("-fx-text-fill: " + fontColor + ";");
                    }
                }
                else {
                    // default fot empty
                    cellPane.getStyleClass().add("cellDefault");

                    // default fot empty
                    label.getStyleClass().add("labelDefault");

                    // default fot empty
                    alignment  = "LEFT";
                }


                switch (alignment) {
                    case "LEFT":
                        label.setAlignment(Pos.CENTER_LEFT);
                        break;
                    case "CENTER":
                        label.setAlignment(Pos.CENTER);
                        break;
                    case "RIGHT":
                        label.setAlignment(Pos.CENTER_RIGHT);
                        break;
                }


                // mouse events for select, drag and release
                addMouseEvents(cellPane, coordinate);

                sheetGridPane.add(cellPane, col, rowIndex + 1); // adding cell
            }
        }


        rowIndexMap = new HashMap<>();
        int originalRowIndex;
        //map for linking where is a row in printing order
        for (int displayedRowIndex = 0; displayedRowIndex < sortedRowOrder.size(); displayedRowIndex++) {
            originalRowIndex = sortedRowOrder.get(displayedRowIndex);
            rowIndexMap.put(originalRowIndex, displayedRowIndex + 1);
        }

        sheetGridPane.setGridLinesVisible(true); // showing lines of grid


        // for existing previous selection - reselect even after update
        if (startCoordinate!= null && endCoordinate!= null) {
            highlightSelectedRange(endCoordinate, endCoordinate);
        }

    }

    //sets the dto of sheet to be displayed
    public void setPresentedSheet(SheetDto sheetDto) {
        DisplayedSheet = sheetDto;
        updateSheet();
    }






    // Selection and Highlighting:

    private void highlightSelectedRange(Coordinate start, Coordinate end) {
        // cleaning previous selection
        overlayMap.values().forEach(overlay -> {
            // removes style of selection from css (deleting previous selection if exists)
            overlay.getStyleClass().removeAll("highlighted-pane", "highlighted-influence", "highlighted-dependency");
            // adding default style for non selection
            overlay.getStyleClass().add("default-cell");
        });
        // calculating range
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startCol = Math.min(start.getColumn(), end.getColumn());
        int endCol = Math.max(start.getColumn(), end.getColumn());

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Coordinate coordinate = CoordinateCache.createCoordinate(row, col);
                Pane overlay = overlayMap.get(coordinate);
                if (overlay != null) {
                    overlay.getStyleClass().remove("default-cell"); // remove all the styles
                    overlay.getStyleClass().add("highlighted-pane"); // adding the selected style
                }
            }
        }

        if (start == end)
        {
            highlightDependencies();
        }

    }

    // according to og range - to support selection after sorting
    private void highlightSelectedRangeAccordingToOgRange(Coordinate start, Coordinate end) {
        // reset selection
        overlayMap.values().forEach(overlay -> {
            overlay.getStyleClass().removeAll("highlighted-pane", "highlighted-influence", "highlighted-dependency");
            overlay.getStyleClass().add("default-cell");
        });

        // calculating physical range
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startCol = Math.min(start.getColumn(), end.getColumn());
        int endCol = Math.max(start.getColumn(), end.getColumn());

        // for every num in og physical range
        for (int originalRowNum = startRow; originalRowNum <= endRow; originalRowNum++) {
            // calculating the row according to row order
            int gridRowDataIndex = sortedRowOrder.indexOf(originalRowNum);
            if (gridRowDataIndex == -1) {
                // אם השורה לא נמצאת ב-sortedRowOrder, נזרוק חריגה
                throw new RuntimeException("Row " + originalRowNum + " is not currently displayed.");
            }

            // adjusting to grid pane
            int gridRowIndex = gridRowDataIndex + 1; // adding 1 becouse of headers
            for (int col = startCol; col <= endCol; col++) {
                // ignoring header (0)
                if (col == 0) continue;

                // getting stack panes
                Pane overlay = overlayMap.get(CoordinateCache.createCoordinate(gridRowIndex, col));

                if (overlay != null) {
                    overlay.getStyleClass().remove("default-cell"); // remove design
                    overlay.getStyleClass().add("highlighted-pane"); // adding selection design
                }
            }
        }

    }

    public void highlightFunctionRange(String rangeName) {
        BoundariesDto currBoundaries = DisplayedSheet.getRanges().get(rangeName).getBoundaries();
        Coordinate from = CoordinateCache.createCoordinateFromString(currBoundaries.getFrom());
        Coordinate to = CoordinateCache.createCoordinateFromString(currBoundaries.getTo());

        highlightSelectedRangeAccordingToOgRange(from, to); // works with "scattered" range after sorting
    }

    private void highlightDepCoordinate(Coordinate curr) {
        Pane overlay = overlayMap.get(curr);
        if (overlay != null) {// deselect
            overlay.getStyleClass().removeAll("highlighted-influence", "highlighted-pane", "default-cell");
            // select
            overlay.getStyleClass().add("highlighted-dependency");}
    }

    private void highlightInfCoordinate(Coordinate curr) {
        Pane overlay = overlayMap.get(curr);
        if (overlay != null) {
            // deselect
            overlay.getStyleClass().removeAll("highlighted-dependency", "highlighted-pane", "default-cell");
            // select
            overlay.getStyleClass().add("highlighted-influence");}
    }

    public void highlightDependencies() {

        if(!isReadOnly)
        {
            CellDto cell = DisplayedSheet.getCell(getSelectedCoordinate());
            if (cell != null) {
                List<Coordinate> dep = cell.getDependsOn();
                List<Coordinate> inf = cell.getInfluencingOn();

                for (Coordinate depCoord : dep) {
                    highlightDepCoordinate(getDisplayedCellPosition(depCoord));
                }

                for (Coordinate infCoord : inf) {
                    highlightInfCoordinate(getDisplayedCellPosition(infCoord));
                }
            }
        }
    }




    // Range and selection setters:

    private void setSelectedRange(Coordinate topLeft, Coordinate bottomRight) {
        selectedRange.set(new CellRange(topLeft, bottomRight));  // creating new range and updating Property
    }

    public void reSelect(String Topleft, String Bottomright) throws Exception{
        Coordinate topLeftCoor = CoordinateCache.createCoordinateFromString(Topleft);
        Coordinate bottomRightCoor = CoordinateCache.createCoordinateFromString(Bottomright);



        topLeftCoor = getDisplayedCellPosition(topLeftCoor);
        bottomRightCoor = getDisplayedCellPosition(bottomRightCoor);


        if(topLeftCoor!= null && bottomRightCoor != null) {
            setSelectedRange(topLeftCoor, bottomRightCoor); //sets it as it is shown on grid
            highlightSelectedRange(topLeftCoor,bottomRightCoor );
        }
        else {
            throw new IllegalArgumentException("One or both not on grid");
        }

    };





    // Selected Range and Cells getters:

    // returns the coordinate for label (the cell represented in coordinate)
    private Coordinate getCoordinateForLabel(Label label) {
        // gets row and col presented in grid pane
        Integer displayedRow = GridPane.getRowIndex(label.getParent());
        Integer column = GridPane.getColumnIndex(label.getParent());

        if (displayedRow > 0) {
            // translates from row in grid to the presented row according to printing order
            int originalRow = sortedRowOrder.get(displayedRow - 1);

            // return og coordinate
            return CoordinateCache.createCoordinate(originalRow, column);
        } else {
            // in case of error or title col/row
            return CoordinateCache.createCoordinate(sortedRowOrder.get(0), column);
        }
    }

    // returns stack pane according to coordinate
    private StackPane getCellPaneByCoordinate(int row, int col) {
        for (Node node : sheetGridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                if (node instanceof StackPane) {
                    return (StackPane) node;
                }
            }
        }
        return null;
    }

    // returns label according to coordinate
    private Label getLabelByCoordinate(int row, int col) {
        StackPane cellPane = getCellPaneByCoordinate(row, col);
        if (cellPane != null && !cellPane.getChildren().isEmpty()) {
            return (Label) cellPane.getChildren().get(1);
        }
        return null;
    }

    // returns coordinate of selected cell
    public Coordinate getSelectedCoordinate() {
        return selectedCoordinate;  // returns coordinate of selected cell
    }

    // returns Property of selected cell
    public ObjectProperty<Label> selectedCellProperty() {
        return selectedCell; // returns Property of selected cell
    }

    // Getter selectedRangeProperty
    public ObjectProperty<CellRange> selectedRangeProperty() {
        return selectedRange;
    }

    private List<Integer> getRowsFromCoordinates(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rowsInRange = new ArrayList<>();

        // runs on every row in range
        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            // according to index in sortedRowOrder
            if (gridRow - 1 < sortedRowOrder.size()) {
                int actualRow = sortedRowOrder.get(gridRow - 1); // og row according to sortedRowOrder
                rowsInRange.add(actualRow);
            }
        }
        return rowsInRange;
    }

    public String getTopLeft(){
        return coordinateToString(selectedRange.get().getTopLeft());
    }

    public String getBottomRight(){
        return coordinateToString(selectedRange.get().getBottomRight());
    }

    public List<String> getSelectedColumns() {
        // if one of points is not initialized
        if (startCoordinate == null || endCoordinate == null) {
            return new ArrayList<>(); // returning empty list
        }

        // starting and ending col
        int startColumn = startCoordinate.getColumn();
        int endColumn = endCoordinate.getColumn();

        // correct the order
        if (startColumn > endColumn) {
            int temp = startColumn;
            startColumn = endColumn;
            endColumn = temp;
        }

        // new list
        List<String> selectedColumns = new ArrayList<>();

        // adding all cols
        for (int col = startColumn; col <= endColumn; col++) {
            selectedColumns.add(convertColumnNumberToString(col));
        }

        return selectedColumns;
    }

    // Public - sending the list of selected rows without access to lists
    public List<Integer> getSelectedRows() {
        return getRowsInRange(startCoordinate, endCoordinate);
    }

    // Private - calculated rows in range according to coordinated
    private List<Integer> getRowsInRange(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rows = new ArrayList<>();
        int startRow = Math.min(topLeft.getRow(), bottomRight.getRow());
        int endRow = Math.max(topLeft.getRow(), bottomRight.getRow());

        for (int gridRowIndex = startRow; gridRowIndex <= endRow; gridRowIndex++) {
            // translateing according to  sortedRowOrder
            if (gridRowIndex - 1 < sortedRowOrder.size()) {
                int actualRowNumber = sortedRowOrder.get(gridRowIndex - 1);
                rows.add(actualRowNumber);
            }
        }
        return rows;
    }

    // finds the presented coordinate according to physicsl coordiante
    public Coordinate getDisplayedCellPosition(Coordinate originalCoord) {

        // indexes
        Integer displayedRowIndex = rowIndexMap.get(originalCoord.getRow());
        Integer displayedColumnIndex = originalCoord.getColumn();

        // if row not present -  null
        if (displayedRowIndex == null) {
            return null;
        }

        // placement of coordinate according to one displayed
        return CoordinateCache.createCoordinate(displayedRowIndex, displayedColumnIndex);

    }

    public Coordinate actualCellPlacedOnGrid(Coordinate placeOnGrid) {
        return (CoordinateCache.createCoordinate(sortedRowOrder.get(placeOnGrid.getRow() - 1), placeOnGrid.getColumn()));
    }




    // Get Sheet info

    public String getSelectedCoordinateOriginalValue() {
        CellDto cell = DisplayedSheet.getCell(getSelectedCoordinate());
        if(cell != null)
        {
            return cell.getOriginalValue();
        }
        return "";
    }

    public int getLastUpdatedVersion() {

        CellDto cell = DisplayedSheet.getCell(getSelectedCoordinate());
        if(cell != null)
        {
            return cell.getVersion();
        }

        return 0;
    }

    public String getLastUserUpdatedCell() {

        CellDto cell = DisplayedSheet.getCell(getSelectedCoordinate());
        if(cell != null)
        {
            return cell.getLastUserUpdated();
        }

        return "";
    }

    public List<Integer> getVersionList()
    {
        return DisplayedSheet.getNumCellChangedHistory();
    }

    public Map<String, RangeDto> getRanges()
    {
        return DisplayedSheet.getRanges();
    }

    public Map<String, List<String>> getUniqueValuesInRange(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rows = getRowsFromCoordinates(topLeft, bottomRight);
        return DisplayedSheet.getUniqueValuesInRange(rows,getSelectedColumns());
    }

    public int getCurrentSheetVersion()
    {
        return DisplayedSheet.getVersion();
    }

    public String getCellValue(String cell) {
        String value =  DisplayedSheet.getCell(CoordinateCache.createCoordinateFromString(cell)).getValue();
        if (value!=null) {
            return value;
        }
        else {
            return "";
        }
    }




    // Updating:

    public void resetSorting() {

        sortedRowOrder = DisplayedSheet.resetSoretedOrder();

    }

    public void loadSheetCurrent()
    {
        updateSheet();
    }

    //sorting rows in a selected range
    public void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight, List<Character> colList) {

        // converting physical selected range to range according to sorted order
        List<Integer> rowsInRange = getRowsFromCoordinates(topLeft, bottomRight);

        List<Integer> rangeSorted = DisplayedSheet.sortRowsByColumns(rowsInRange, colList);

        // returning the sorted rows from the range to the right placement
        replaceSortedRangeInOrder(rowsInRange, rangeSorted);

        // updating list
        lastSortedOrderBeforeFiltering = new ArrayList<>(sortedRowOrder);
    }

    // returns the sorted rows to the right position in sortedRowOrder
    private void replaceSortedRangeInOrder(List<Integer> originalRange, List<Integer> sortedRange) {
        int startIndex = -1;

        // finding starting index in sortedRowOrder
        for (int i = 0; i < sortedRowOrder.size(); i++) {
            if (sortedRowOrder.get(i).equals(originalRange.get(0))) {
                startIndex = i;
                break;
            }
        }

        // checking for the right placement
        if (startIndex != -1) {
            // replacing the selected range with updated list
            for (int i = 0; i < sortedRange.size(); i++) {
                sortedRowOrder.set(startIndex + i, sortedRange.get(i));
            }
        }
    }









    // Filtering

    public void removeRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight) {
        // searching for rows to remove according to value in col
        List<Integer> rowsToRemove = new ArrayList<>();

        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            int actualRow = lastSortedOrderBeforeFiltering.get(gridRow - 1); // translate to row in sortedRowOrder
            Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, convertColumnNameToNumber(columnName));
            CellDto cell = DisplayedSheet.getCell(coordinate);
            if (cell != null && cell.getValue().equals(value)) {
                // updating count of reasons to remve
                removalCountMap.put(actualRow, removalCountMap.getOrDefault(actualRow, 0) + 1);
                rowsToRemove.add(actualRow);
            }
        }

        // removing from sorted order
        sortedRowOrder.removeAll(rowsToRemove);

        // updating ui
        updateSheet();
    }

    public void addRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight) {
        // list according to currrent order
        List<Integer> rowsInRange = getRowsFromCoordinatesBeforeFiltering(topLeft, bottomRight);

        // looking for all rows to add
        List<Integer> rowsToAdd = new ArrayList<>();

        // on all rows translated
        for (Integer actualRow : rowsInRange) {
            Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, convertColumnNameToNumber(columnName));
            CellDto cell = DisplayedSheet.getCell(coordinate);

            if (cell != null && cell.getValue().equals(value)) {
                // updating count of reasons for removal
                int count = removalCountMap.getOrDefault(actualRow, 0);
                if (count > 0) {
                    removalCountMap.put(actualRow, count - 1);
                }

                // if all the count is 0 ie all reasons for removal dont exist anymore - bring back row
                if (removalCountMap.get(actualRow) == 0) {
                    removalCountMap.remove(actualRow); // removing from map
                    rowsToAdd.add(actualRow);
                }
            }
        }

        // adding back rows according to og place
        for (Integer row : rowsToAdd) {
            insertRowInCorrectOrder(row);
        }

        // updating ui
        updateSheet();
    }

    // adding back rows according to og placement with lastSortedOrder
    private void insertRowInCorrectOrder(Integer row) {
        if (lastSortedOrderBeforeFiltering == null) {
            // if no sorted order bring row back in chronological order
            for (int i = 0; i < sortedRowOrder.size(); i++) {
                if (sortedRowOrder.get(i) > row) {
                    sortedRowOrder.add(i, row);
                    return;
                }
            }
            sortedRowOrder.add(row); // didnt find placement - putting in the end
            return;
        }

        // if there is sorted order find the placer in lastSortedOrder
        int indexInLastSorted = lastSortedOrderBeforeFiltering.indexOf(row);
        for (int i = 0; i < sortedRowOrder.size(); i++) {
            int currentRowInOrder = sortedRowOrder.get(i);
            if (lastSortedOrderBeforeFiltering.indexOf(currentRowInOrder) > indexInLastSorted) {
                sortedRowOrder.add(i, row);
                return;
            }
        }

        // didnt find placement - putting in the end
        sortedRowOrder.add(row);
    }

    private List<Integer> getRowsFromCoordinatesBeforeFiltering(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rowsInRange = new ArrayList<>();

        // all rows in selected range
        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            //  according to sortedRowOrder
            if (gridRow - 1 < lastSortedOrderBeforeFiltering.size()) {
                int actualRow = lastSortedOrderBeforeFiltering.get(gridRow - 1); // adding the row to list
                rowsInRange.add(actualRow);
            }
        }

        return rowsInRange;
    }




    // Width and Height:

    // width of selected cell
    public double getCellWidth() {
        String columnName = convertColumnNumberToString(selectedCoordinate.getColumn());
        return columnWidth.getOrDefault(columnName, cellWidth);
    }

    // height of selected cell
    public double getCellHeight() {
        Integer rowName = (selectedCoordinate.getRow());
        return rowHeight.getOrDefault(rowName, cellHeight);
    }

    // avg width of selected cells
    public double getAverageCellWidth() {
        List<String> columnsInRange = getSelectedColumns();
        double totalWidth = 0;
        for (String columnName : columnsInRange) {
            totalWidth += columnWidth.getOrDefault(columnName, cellWidth);
        }
        return totalWidth / columnsInRange.size();
    }

    // avg height of selected cells
    public double getAverageCellHeight() {
        List<Integer> rowsInRange = getRowsInRange(selectedRange.getValue().getTopLeft(), selectedRange.get().getBottomRight());
        double totalHeight = 0;
        for (Integer rowName : rowsInRange) {
            totalHeight += rowHeight.getOrDefault(rowName, cellHeight);
        }
        return totalHeight / rowsInRange.size();
    }

    public void updateColWidth(double newWidth) {
        if (selectedCoordinate != null) {
            int columnIndex = selectedCoordinate.getColumn();

            // updating ColumnConstraints for selected col
            if (columnIndex >= 0 && columnIndex < sheetGridPane.getColumnConstraints().size()) {
                ColumnConstraints colConstraints = sheetGridPane.getColumnConstraints().get(columnIndex);
                colConstraints.setPrefWidth(newWidth);
                colConstraints.setMinWidth(newWidth);
                colConstraints.setMaxWidth(newWidth);

                // updating columnWidth map
                String columnName = convertColumnNumberToString(columnIndex);
                columnWidth.put(columnName, newWidth);
            } else {
                System.out.println("Index out of bounds for column constraints.");
            }
        } else if (selectedRange.get() != null) {
            // updating width cols in range
            Coordinate topLeft = selectedRange.get().getTopLeft();
            Coordinate bottomRight = selectedRange.get().getBottomRight();

            int startColumn = Math.min(topLeft.getColumn(), bottomRight.getColumn());
            int endColumn = Math.max(topLeft.getColumn(), bottomRight.getColumn());

            for (int columnIndex = startColumn; columnIndex <= endColumn; columnIndex++) {
                if (columnIndex >= 0 && columnIndex < sheetGridPane.getColumnConstraints().size()) {
                    ColumnConstraints colConstraints = sheetGridPane.getColumnConstraints().get(columnIndex);
                    colConstraints.setPrefWidth(newWidth);
                    colConstraints.setMinWidth(newWidth);
                    colConstraints.setMaxWidth(newWidth);

                    // updating columnWidth map
                    String columnName = convertColumnNumberToString(columnIndex);
                    columnWidth.put(columnName, newWidth);
                } else {
                    System.out.println("Index out of bounds for column constraints at column " + columnIndex);
                }
            }
        } else {
            System.out.println("No cell or range is selected.");
        }
    }

    public void updateRowHeight(double newHeight) {
        if (selectedCoordinate != null) {
            int uiRowIndex = selectedCoordinate.getRow(); // index ot selected rows
            if (uiRowIndex == 0) {
                System.out.println("Cannot change height of header row.");
                return;
            }

            int gridRowIndex = uiRowIndex;
            int gridRowDataIndex = gridRowIndex - 1; // adjusting to find correct index

            if (gridRowIndex >= 0 && gridRowIndex < sheetGridPane.getRowConstraints().size()) {
                // finding correct place according to sortedRowOrder
                if (gridRowDataIndex >= 0 && gridRowDataIndex < sortedRowOrder.size()) {
                    int actualRowNumber = sortedRowOrder.get(gridRowDataIndex);

                    // updating RowConstraints for selected row
                    RowConstraints rowConstraints = sheetGridPane.getRowConstraints().get(gridRowIndex);
                    rowConstraints.setPrefHeight(newHeight);
                    rowConstraints.setMinHeight(newHeight);
                    rowConstraints.setMaxHeight(newHeight);

                    // updating rowHeight
                    rowHeight.put(actualRowNumber, newHeight);
                } else {
                    System.out.println("Index out of bounds for sortedRowOrder.");
                }
            } else {
                System.out.println("Index out of bounds for row constraints.");
            }
        } else if (selectedRange.get() != null) {
            // in range
            Coordinate topLeft = selectedRange.get().getTopLeft();
            Coordinate bottomRight = selectedRange.get().getBottomRight();

            // getting list of acual rows from getRowsInRange
            List<Integer> rowsToUpdate = getRowsInRange(topLeft, bottomRight);

            // foe every row find placement in GridPane
            for (Integer actualRowNumber : rowsToUpdate) {
                int gridRowDataIndex = sortedRowOrder.indexOf(actualRowNumber);
                if (gridRowDataIndex != -1) {
                    int gridRowIndex = gridRowDataIndex + 1; // adjusting according to index in list

                    if (gridRowIndex >= 0 && gridRowIndex < sheetGridPane.getRowConstraints().size()) {
                        //  RowConstraints
                        RowConstraints rowConstraints = sheetGridPane.getRowConstraints().get(gridRowIndex);
                        rowConstraints.setPrefHeight(newHeight);
                        rowConstraints.setMinHeight(newHeight);
                        rowConstraints.setMaxHeight(newHeight);

                        //  rowHeight
                        rowHeight.put(actualRowNumber, newHeight);
                    } else {
                        System.out.println("Index out of bounds for row constraints at row " + gridRowIndex);
                    }
                } else {
                    System.out.println("Row " + actualRowNumber + " not found in sortedRowOrder.");
                }
            }
        } else {
            System.out.println("No cell or range is selected.");
        }
    }




    // Convert:

    private String convertColumnNumberToString(int columnNumber) {
        StringBuilder columnName = new StringBuilder();
        while (columnNumber > 0) {
            int remainder = (columnNumber - 1) % 26;
            columnName.insert(0, (char)(remainder + 'A'));
            columnNumber = (columnNumber - 1) / 26;
        }
        return columnName.toString();
    }

    private String coordinateToString(Coordinate coordinate) {
        int column = coordinate.getColumn();
        char columnLetter = (char) ('A' + column - 1);

        int row = coordinate.getRow();

        return String.valueOf(columnLetter) + row;
    }

    private int convertColumnNameToNumber(String columnName) {
        int columnNumber = 0;

        for (int i = 0; i < columnName.length(); i++) {
            char currentChar = columnName.charAt(i);

            columnNumber = columnNumber * 26 + (currentChar - 'A' + 1);
        }

        return columnNumber;
    }



    // Read mode:

    public void setReadOnly(boolean readOnly) {
        for (Node node : sheetGridPane.getChildren()) {
            node.setMouseTransparent(readOnly); // מבטל אינטראקציות עכבר
        }
        isReadOnly = true;
    }

}
