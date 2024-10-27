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


    SheetDto DisplayedSheet = null;

    private ObjectProperty<Label> selectedCell;

    private UImodel uiModel;

    private Coordinate selectedCoordinate; // משתנה שומר על הקואורדינטה הנבחרת



    // SheetEngine sheetEngine = new SheetEngineImpl();       להפטר כשהמעבר הושלם!



    private boolean readOnly = false;

    private final double cellWidth = 100.0; // רוחב קבוע לכל תא
    private final double cellHeight = 40.0; // גובה קבוע לכל תא


    public ScrollPane sheetScrollPane;
    @FXML
    private GridPane sheetGridPane;

    @FXML
    private Label leftLabel;
    @FXML
    private Label rightLabel;



    private List<Integer> sortedRowOrder; // printing list


    private final Map<Coordinate, Pane> overlayMap = new HashMap<>();

    // רשימה שמורה של הסדר האחרון המלא לפני ביצוע סינון
    private List<Integer> lastSortedOrderBeforeFiltering;

    // מיפוי בין אינדקסי שורות מקוריים לאינדקסים מוצגים
    Map<Integer, Integer> rowIndexMap;

    // קואורדינטה של התא שממנו התחלנו את הבחירה
    private Coordinate startCoordinate;

    // קואורדינטה של התא בו עזבנו את הבחירה
    private Coordinate endCoordinate;

    // הוספת Property עבור טווח נבחר
    private ObjectProperty<CellRange> selectedRange = new SimpleObjectProperty<>();

    // מפת מעקב אחרי מספר העמודות שבגללן כל שורה הוסרה
    private Map<Integer, Integer> removalCountMap = new HashMap<>();

    // הוספת משתנה דגל
    private boolean isDragging = false;


    private Map<String, Double> columnWidth = new HashMap<>();
    private Map<Integer, Double> rowHeight = new HashMap<>();


    private AnimationTimer scrollTimer;
    private static final double SCROLL_SPEED = 0.01; // מהירות הגלילה
    private static final double EDGE_THRESHOLD = 20; // מרחק מקצה ה-ScrollPane שבו מתחילה הגלילה
    private double scrollDirectionX = 0;
    private double scrollDirectionY = 0;

    private boolean isReadOnly = false;



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
                if(!isReadOnly)
                {
                    selectedCoordinate = getCoordinateForLabel(newSelectedLabel);  // עדכון הקואורדינטה עם השורה המקורית
                }
            }
        });

        //populateGrid(); // יצירת תאים והוספתם לגריד



        // הוספת מאזין לאירועי העכבר על ה-ScrollPane
        sheetScrollPane.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMove);
        sheetScrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseMove);

        // יצירת AnimationTimer לגלילה
        scrollTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (scrollDirectionX != 0 || scrollDirectionY != 0) {
                    double hValue = sheetScrollPane.getHvalue() + scrollDirectionX * SCROLL_SPEED;
                    double vValue = sheetScrollPane.getVvalue() + scrollDirectionY * SCROLL_SPEED;

                    // הבטחת הערכים בין 0 ל-1
                    hValue = Math.max(0, Math.min(hValue, 1));
                    vValue = Math.max(0, Math.min(vValue, 1));

                    sheetScrollPane.setHvalue(hValue);
                    sheetScrollPane.setVvalue(vValue);
                }
            }
        };


    }




    private void handleMouseMove(MouseEvent event) {

        Bounds viewportBounds = sheetScrollPane.getViewportBounds();
        Bounds contentBounds = sheetScrollPane.getContent().getLayoutBounds();

        // קבלת המיקום היחסי של העכבר בתוך ה-ScrollPane
        double mouseX = event.getX();
        double mouseY = event.getY();

        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();

        // בדיקה אם העכבר קרוב לקצה הימני או השמאלי
        if (mouseX <= EDGE_THRESHOLD && isDragging) {
            scrollDirectionX = -1; // גלילה שמאלה
        } else if (mouseX >= viewportWidth - EDGE_THRESHOLD && isDragging) {
            scrollDirectionX = 1; // גלילה ימינה
        } else {
            scrollDirectionX = 0;
        }

        // בדיקה אם העכבר קרוב לקצה העליון או התחתון
        if (mouseY <= EDGE_THRESHOLD && isDragging) {
            scrollDirectionY = -1; // גלילה למעלה
        } else if (mouseY >= viewportHeight - EDGE_THRESHOLD && isDragging) {
            scrollDirectionY = 1; // גלילה למטה
        } else {
            scrollDirectionY = 0;
        }

        // אם צריך לגלול, נתחיל את ה-scrollTimer
        if (scrollDirectionX != 0 || scrollDirectionY != 0) {
            scrollTimer.start();
        } else {
            scrollTimer.stop();
        }





        // אם העכבר נגרר, נעדכן את הקואורדינטה הסופית ונעדכן את הסימון
        if (event.isPrimaryButtonDown()) {
            // חישוב הקואורדינטה הנוכחית של העכבר ב-GridPane
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
            Point2D mouseGridCoords = sheetGridPane.sceneToLocal(mouseSceneCoords);

            int col = getColumnIndexAtX(mouseGridCoords.getX());
            int row = getRowIndexAtY(mouseGridCoords.getY());

            if (col >= 1 && row >= 1) { // התעלמות מכותרות
                Coordinate currentCoordinate = CoordinateCache.createCoordinate(row, col);
                if (!currentCoordinate.equals(endCoordinate)) {
                    endCoordinate = currentCoordinate;
                    Platform.runLater(() -> highlightSelectedRange(startCoordinate, endCoordinate));
                    setSelectedRange(startCoordinate, endCoordinate);
                }
            }
        }

    }

    private int getColumnIndexAtX(double x) {
        double accumulatedWidth = 0;
        for (int i = 0; i < sheetGridPane.getColumnConstraints().size(); i++) {
            accumulatedWidth += sheetGridPane.getColumnConstraints().get(i).getPrefWidth();
            if (x < accumulatedWidth) {
                return i;
            }
        }
        return -1;
    }

    private int getRowIndexAtY(double y) {
        double accumulatedHeight = 0;
        for (int i = 0; i < sheetGridPane.getRowConstraints().size(); i++) {
            accumulatedHeight += sheetGridPane.getRowConstraints().get(i).getPrefHeight();
            if (y < accumulatedHeight) {
                return i;
            }
        }
        return -1;
    }




    // פונקציה שמחזירה את הקואורדינטה המקורית עבור תווית נבחרת (label)
    private Coordinate getCoordinateForLabel(Label label) {
        // קבלת השורה והעמודה המוצגת ב-GridPane
        Integer displayedRow = GridPane.getRowIndex(label.getParent());
        Integer column = GridPane.getColumnIndex(label.getParent());

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
        for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof StackPane)) {
            StackPane cellPane = (StackPane) node;
            Label label = (Label) cellPane.getChildren().get(0);
            label.setAlignment(alignment);
        }
    }

    // פונקציה לסימון תאים שתלויים אחד בשני
    @Override
    public void markCellsButtonActionListener(boolean isMarked) {
        String color = isMarked ? "yellow" : "white";
        for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof StackPane)) {
            StackPane cellPane = (StackPane) node;
            cellPane.setStyle("-fx-background-color: " + color + ";");
        }
    }

    @Override
    public void toggleCellColor(boolean isSelected) {
        String color = isSelected ? "red" : "white";
        for (Node node : sheetGridPane.getChildren().filtered(node -> node instanceof StackPane)) {
            StackPane cellPane = (StackPane) node;
            cellPane.setStyle("-fx-background-color: " + color + ";");
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
        String backgroundColor;
        String fontColor;
        String alignment;
        // ניקוי ה-GridPane הקיים
        sheetGridPane.getChildren().clear();
        sheetGridPane.setGridLinesVisible(false); // הצגת קווי ההפרדה
        sheetGridPane.setHgap(0); // מרווח אופקי אפס
        sheetGridPane.setVgap(0); // מרווח אנכי אפס


        startCoordinate = CoordinateCache.createCoordinate(1,1);



        // קביעת גודל הגריד לפי מספר השורות והעמודות של הגיליון
        sheetGridPane.getRowConstraints().clear();
        sheetGridPane.getColumnConstraints().clear();


        if (sortedRowOrder == null) {
            sortedRowOrder = DisplayedSheet.resetSoretedOrder();
        }

        if (lastSortedOrderBeforeFiltering == null) {
            lastSortedOrderBeforeFiltering = new ArrayList<>();
            lastSortedOrderBeforeFiltering.addAll(sortedRowOrder);
        }

        // הוספת כותרות עמודות
        for (int col = 0; col < DisplayedSheet.getNumOfColumns(); col++) {
            char columnLetter = (char) ('A' + col); // A, B, C וכו'
            Label columnHeader = new Label(String.valueOf(columnLetter));
            columnHeader.getStyleClass().add("sheet-header-text");
            columnHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // החלת רקע ורוד
            sheetGridPane.add(columnHeader, col + 1, 0);
            columnHeader.getStyleClass().add("sheet-header-background");

            // הוספת אירועי עכבר לכותרת העמודה
            addMouseEventsToColumnHeader(columnHeader, col + 1);

        }

        // הוספת כותרות שורות לפי הסדר המודפס
        for (int rowIndex = 0; rowIndex < sortedRowOrder.size(); rowIndex++) {
            int actualRow = sortedRowOrder.get(rowIndex);  // שורה לפי הסדר המודפס
            Label rowHeader = new Label(String.valueOf(actualRow)); // הדפסה לפי מספר השורה המקורי
            rowHeader.getStyleClass().add("sheet-header-text");
            rowHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // החלת רקע ורוד
            sheetGridPane.add(rowHeader, 0, rowIndex + 1); // מיקום השורה +1 כי שורה 0 לכותרות
            rowHeader.getStyleClass().add("sheet-header-background");

            // הוספת אירועי עכבר לכותרת השורה
            addMouseEventsToRowHeader(rowHeader, rowIndex + 1);
        }



// קביעת גודל השורות

// הוספת הגדרות עבור שורת הכותרת (row == 0)
        RowConstraints headerRowConstraints = new RowConstraints();
        headerRowConstraints.setPrefHeight(cellHeight);
        headerRowConstraints.setMinHeight(cellHeight);
        headerRowConstraints.setMaxHeight(cellHeight);
        sheetGridPane.getRowConstraints().add(headerRowConstraints);

// הוספת הגדרות עבור השורות המוצגות
        for (int i = 0; i < sortedRowOrder.size(); i++) {
            RowConstraints rowConstraints = new RowConstraints();

            int rowNumber = sortedRowOrder.get(i);

            // קבלת הגובה של השורה מהמפה, אם לא קיים משתמשים ב-cellHeight
            double height = rowHeight.getOrDefault(rowNumber, cellHeight);

            rowConstraints.setPrefHeight(height);
            rowConstraints.setMinHeight(height);
            rowConstraints.setMaxHeight(height);

            sheetGridPane.getRowConstraints().add(rowConstraints);
        }




        // קביעת גודל העמודות

// הוספת הגדרות עבור שורת הכותרת (row == 0)
        ColumnConstraints headerColConstraints = new ColumnConstraints();
        headerColConstraints.setPrefWidth(cellWidth);
        headerColConstraints.setPrefWidth(cellWidth);
        headerColConstraints.setPrefWidth(cellWidth);
        sheetGridPane.getColumnConstraints().add(headerColConstraints);

// הוספת הגדרות עבור השורות המוצגות
        for (int col = 1; col <= DisplayedSheet.getNumOfColumns(); col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();

            // קבלת שם העמודה (בהנחה שהעמודות מתחילות מ-1)
            String colKey = convertColumnNumberToString(col);

            // קבלת הרוחב של העמודה מהמפה, אם לא קיים משתמשים ב-cellWidth
            double width = columnWidth.getOrDefault(colKey, cellWidth);

            colConstraints.setPrefWidth(width);
            colConstraints.setMinWidth(width);
            colConstraints.setMaxWidth(width);

            sheetGridPane.getColumnConstraints().add(colConstraints);
        }
        /*
// קביעת גודל העמודות
        for (int col = 0; col <= sheetDto.getNumOfColumns(); col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();

            // קבלת שם העמודה (בהנחה שהעמודות מתחילות מ-1)
            String colKey = convertColumnNumberToString(col + 1);

            // קבלת הרוחב של העמודה מהמפה, אם לא קיים משתמשים ב-cellWidth
            double width = columnWidth.getOrDefault(colKey, cellWidth);

            colConstraints.setPrefWidth(width);
            colConstraints.setMinWidth(width);
            colConstraints.setMaxWidth(width);

            sheetGridPane.getColumnConstraints().add(colConstraints);
        }
         */





        // הוספת התאים לפי סדר השורות המודפסות
        for (int rowIndex = 0; rowIndex < sortedRowOrder.size(); rowIndex++) {
            int actualRow = sortedRowOrder.get(rowIndex);  // שורה לפי הסדר המודפס
            for (int col = 1; col <= DisplayedSheet.getNumOfColumns(); col++) {
                Coordinate coordinate = CoordinateCache.createCoordinate(rowIndex + 1, col); // coordinate according to gridpane
                CellDto cell = DisplayedSheet.getCell(actualRow, col);



                // יצירת StackPane עבור התא
                StackPane cellPane = new StackPane();
                cellPane.setPadding(Insets.EMPTY); // הסרת Padding מה-cellPane


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

                // הגדרת התווית כך שתהיה שקופה
                label.setBackground(Background.EMPTY);








                // יצירת Pane שישמש כשכבת סימון
                Pane selectionOverlay = new Pane();
                selectionOverlay.setPadding(Insets.EMPTY);

                selectionOverlay.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                selectionOverlay.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
                selectionOverlay.setPrefWidth(Double.MAX_VALUE);
                selectionOverlay.setPrefHeight(Double.MAX_VALUE);


                selectionOverlay.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                selectionOverlay.setMouseTransparent(true); // כדי שלא יחסום אירועי עכבר




// שמירת ה-Rectangle במפה כדי לגשת אליו מאוחר יותר
                overlayMap.put(coordinate, selectionOverlay);


                // הוספת ה-Pane ל-cellPane (מעל ה-Label)
                cellPane.getChildren().add(selectionOverlay);


                // הוספת ה-Label ל-cellPane
                cellPane.getChildren().add(label);



                if (cell != null)
                {
                    // **קבלת צבע הרקע מהתא**
                    backgroundColor = cell.getStyle().getBackgroundColor(); // פונקציה שמחזירה את מחרוזת הצבע

                    // **קבלת צבע הפונט מהתא**
                    fontColor = cell.getStyle().getTextColor(); // פונקציה שמחזירה את מחרוזת הצבע לפונט


                    alignment = cell.getStyle().getAlignment();


                    if (Objects.equals(backgroundColor, "#FFFFFF"))
                    {
                        // הגדרת סגנון הרקע של התא עם הצבע שהתקבל
                        cellPane.getStyleClass().add("cellDefault");

                    }
                    else
                    {
                        // הגדרת סגנון הרקע של התא עם הצבע שהתקבל
                        cellPane.setStyle("-fx-background-color: " + backgroundColor + "; -fx-padding: 5px;");

                    }


                    if (Objects.equals(fontColor, "#000000"))
                    {
                        // החלת צבע הפונט של התווית
                        label.getStyleClass().add("labelDefault");
                    }
                    else
                    {
                        // החלת צבע הפונט של התווית
                        label.setStyle("-fx-text-fill: " + fontColor + ";");
                    }
                }
                else {
                    // הגדרת סגנון הרקע של התא עם הצבע שהתקבל
                    cellPane.getStyleClass().add("cellDefault");

                    // החלת צבע הפונט של התווית
                    label.getStyleClass().add("labelDefault");

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











                // הוספת אירועים ללחיצת עכבר, גרירה ושחרור
                addMouseEvents(cellPane, coordinate);

                sheetGridPane.add(cellPane, col, rowIndex + 1); // הצגת התא בשורה החדשה
            }
        }



        rowIndexMap = new HashMap<>();
        int originalRowIndex;
        //map for linking where is a row in printing order
        for (int displayedRowIndex = 0; displayedRowIndex < sortedRowOrder.size(); displayedRowIndex++) {
            originalRowIndex = sortedRowOrder.get(displayedRowIndex);
            rowIndexMap.put(originalRowIndex, displayedRowIndex + 1);
        }

        sheetGridPane.setGridLinesVisible(true); // הצגת קווי ההפרדה
    }





    // הוספת אירועים ללחיצה, גרירה ושחרור עבור תאים
    private void addMouseEvents(StackPane cellPane, Coordinate coordinate) {

        // אירוע לחיצה על עכבר (התחלה)
        cellPane.setOnMousePressed(event -> {
            startCoordinate = coordinate; // שמירת קואורדינטת התחלה
            endCoordinate = coordinate;
            //setSelectedRange(startCoordinate, endCoordinate);
            if(!isReadOnly)
            {
                selectedCell.set(getLabelByCoordinate(startCoordinate.getRow(), startCoordinate.getColumn()));
            }
            highlightSelectedRange(startCoordinate, endCoordinate);
            System.out.println("Mouse pressed at: " + startCoordinate.getRow() + ", " + startCoordinate.getColumn());
        });

        // אירוע שמתחיל גרירה ברגע שהיא מזוהה
        cellPane.setOnDragDetected(event -> {
            cellPane.startFullDrag(); // מתחילים גרירה מלאה
            isDragging = true;
            System.out.println("Drag detected, starting full drag.");
        });

        // אירוע מעבר מעל תא תוך כדי גרירה
        cellPane.setOnMouseDragOver(event -> {
            if (coordinate != endCoordinate) {
                System.out.println("Mouse dragged over: " + coordinate.getRow() + ", " + coordinate.getColumn());
                endCoordinate = coordinate;
                highlightSelectedRange(startCoordinate, endCoordinate); // עידכון הסימון במהלך הגרירה
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

        // אירוע שחרור עכבר
        cellPane.setOnMouseReleased(event -> {
            System.out.println("Mouse released at: " + endCoordinate.getRow() + ", " + endCoordinate.getColumn());
            isDragging = false;
            highlightSelectedRange(startCoordinate, endCoordinate); // סימון הטווח שנבחר
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



/*
    // פונקציה לסימון טווח תאים לפי הגרירה (עבודה על StackPane ולא על Label)
    private void highlightSelectedRange(Coordinate start, Coordinate end) {
        // ניקוי כל התאים מסימון קודם, למעט הכותרות (שורות ועמודות)
        sheetGridPane.getChildren().forEach(node -> {
            if (node instanceof StackPane) {
                StackPane cellPane = (StackPane) node;

                // בדיקת מיקום התא - אם הוא בכותרת עמודה (שורה 0) או בכותרת שורה (עמודה 0)
                Integer row = GridPane.getRowIndex(node);
                Integer col = GridPane.getColumnIndex(node);

                // אם התא הוא לא כותרת עמודה או שורה, נצבע אותו בלבן
                if (row != null && col != null && row > 0 && col > 0) {
                    cellPane.setStyle("-fx-background-color: white; -fx-padding: 5px;");
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
                StackPane cellPane = getCellPaneByCoordinate(row, col);
                if (cellPane != null) {
                    cellPane.setStyle("-fx-background-color: #ffd5e9; -fx-padding: 5px;"); // סימון התא בצבע ורוד
                }
            }
        }
    }

 */



    private void addMouseEventsToColumnHeader(Label columnHeader, int colIndex) {
        columnHeader.setOnMousePressed(event -> {
            isDragging = true;
            startCoordinate = CoordinateCache.createCoordinate(1, colIndex);
            endCoordinate = CoordinateCache.createCoordinate(sheetGridPane.getRowConstraints().size() - 1, colIndex);
            highlightSelectedRange(startCoordinate, endCoordinate);
        });

        columnHeader.setOnMouseDragged(event -> {
            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());
            Point2D mouseGridCoords = sheetGridPane.sceneToLocal(mouseSceneCoords);
            int currentCol = getColumnIndexAtX(mouseGridCoords.getX());

            if (currentCol >= 1 && currentCol <= sheetGridPane.getColumnConstraints().size()) {
                int minCol = Math.min(colIndex, currentCol);
                int maxCol = Math.max(colIndex, currentCol);

                startCoordinate = CoordinateCache.createCoordinate(1, minCol);
                endCoordinate = CoordinateCache.createCoordinate(sheetGridPane.getRowConstraints().size() - 1, maxCol);
                highlightSelectedRange(startCoordinate, endCoordinate);
            }
        });

        columnHeader.setOnMouseReleased(event -> {
            isDragging = false;
            setSelectedRange(startCoordinate, endCoordinate);
        });
    }



    private void addMouseEventsToRowHeader(Label rowHeader, int rowIndex) {
        rowHeader.setOnMousePressed(event -> {
            isDragging = true;
            startCoordinate = CoordinateCache.createCoordinate(rowIndex, 1);
            endCoordinate = CoordinateCache.createCoordinate(rowIndex, sheetGridPane.getColumnConstraints().size() - 1);
            highlightSelectedRange(startCoordinate, endCoordinate);
        });

        rowHeader.setOnMouseDragged(event -> {
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

        rowHeader.setOnMouseReleased(event -> {
            isDragging = false;
            setSelectedRange(startCoordinate, endCoordinate);
        });
    }



    private void highlightSelectedRange(Coordinate start, Coordinate end) {
        // ניקוי הסימון הקודם
        // איפוס הסימון הקודם
        overlayMap.values().forEach(overlay -> {
            // הסרת כל מחלקות ה-CSS שקשורות לסימון כדי להחזיר למצב המקורי
            overlay.getStyleClass().removeAll("highlighted-pane", "highlighted-influence", "highlighted-dependency");
            // הוספת מחלקת CSS ברירת מחדל
            overlay.getStyleClass().add("default-cell");
        });
        // חישוב הטווח
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startCol = Math.min(start.getColumn(), end.getColumn());
        int endCol = Math.max(start.getColumn(), end.getColumn());

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Coordinate coordinate = CoordinateCache.createCoordinate(row, col);
                Pane overlay = overlayMap.get(coordinate);
                if (overlay != null) {
                    overlay.getStyleClass().remove("default-cell"); // הסר את עיצוב ברירת המחדל
                    overlay.getStyleClass().add("highlighted-pane"); // הוספת העיצוב שנבחר
                }
            }
        }

        if (start == end)
        {
            highlightDependencies();
        }

    }






    // פונקציה לסימון טווח תאים לפי הגרירה (עבודה על StackPane ולא על Label)
    private void highlightSelectedRangeAccordingToOgRange(Coordinate start, Coordinate end) {
        // ניקוי כל התאים מסימון קודם, למעט הכותרות (שורות ועמודות)
        overlayMap.values().forEach(overlay -> {
            // הסרת כל מחלקות ה-CSS שקשורות לסימון כדי להחזיר למצב המקורי
            overlay.getStyleClass().removeAll("highlighted-pane", "highlighted-influence", "highlighted-dependency");
            // הוספת מחלקת CSS ברירת מחדל
            overlay.getStyleClass().add("default-cell");
        });
        // סימון טווח חדש
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startCol = Math.min(start.getColumn(), end.getColumn());
        int endCol = Math.max(start.getColumn(), end.getColumn());

        // עבור כל מספר שורה בטווח המקורי
        for (int originalRowNum = startRow; originalRowNum <= endRow; originalRowNum++) {
            // חיפוש אינדקס השורה ב-sortedRowOrder
            int gridRowDataIndex = sortedRowOrder.indexOf(originalRowNum);
            if (gridRowDataIndex == -1) {
                // אם השורה לא נמצאת ב-sortedRowOrder, נזרוק חריגה
                throw new RuntimeException("Row " + originalRowNum + " is not currently displayed.");
            }

            // אינדקס השורה ב-GridPane (התאמה עקב שורת הכותרת)
            int gridRowIndex = gridRowDataIndex + 1; // מוסיפים 1 כי שורת הכותרת היא בשורה 0

            for (int col = startCol; col <= endCol; col++) {
                // התעלמות מהכותרת של העמודה (עמודה 0)
                if (col == 0) continue;

                // קבלת ה-StackPane לפי הקואורדינטות ב-GridPane
                Pane overlay = overlayMap.get(CoordinateCache.createCoordinate(gridRowIndex, col));

                if (overlay != null) {
                    overlay.getStyleClass().remove("default-cell"); // הסר את עיצוב ברירת המחדל
                    overlay.getStyleClass().add("highlighted-pane"); // הוספת העיצוב שנבחר
                }
            }
        }

    }






    // פונקציה שמחזירה את ה-StackPane לפי קואורדינטות
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

    // פונקציה שמחזירה את התווית (Label) לפי קואורדינטות
    private Label getLabelByCoordinate(int row, int col) {
        StackPane cellPane = getCellPaneByCoordinate(row, col);
        if (cellPane != null && !cellPane.getChildren().isEmpty()) {
            return (Label) cellPane.getChildren().get(1);
        }
        return null;
    }


    @Override
    public void updateCellContent(Coordinate coordinate, String content) {

        /*
        try {
            // המרה של הקואורדינטה למחרוזת (לדוגמה: "A1")
            String cellReference = coordinateToString(coordinate);

            // עדכון הערך ב-sheetEngine
            sheetEngine.updateCellValue(cellReference, content);

            // קבלת ה- SheetDto המעודכן
            SheetDto sheetDto = sheetEngine.getCurrentSheetDTO();



            updateSheet(sheetDto);

        } catch (Exception e) {
            // טיפול בשגיאות
            System.err.println("Error updating cell content: " + e.getMessage());
            e.printStackTrace();
        }

         */
    }

    public Coordinate getSelectedCoordinate() {
        return selectedCoordinate;  // החזרת הקואורדינטה של התא הנבחר
    }

    public ObjectProperty<Label> selectedCellProperty() {
        return selectedCell;  // החזרת ה-Property של התא הנבחר
    }




    //delete after everything working!!!!!!!!!!
    @Override
    public void loadSheetFromFile(String filename) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException {
        /*
        sheetEngine.loadSheetFromXML(filename);
        // קביעת גודל השורות והעמודות

        SheetDto sheetDto = sheetEngine.getCurrentSheetDTO();
        for (int row = 0; row < sheetDto.getNumOfRows(); row++) {
            // אתחול המפה rowHeight עם גובה השורה
            rowHeight.put(row, cellHeight);
        }

        for (int col = 0; col < sheetDto.getNumOfColumns(); col++) {
            // המרת אינדקס העמודה לאות (למשל, 0 -> 'A', 1 -> 'B', וכו')
            String columnLetter = convertColumnNumberToString(col);
            // אתחול המפה columnWidth עם רוחב העמודה
            columnWidth.put(columnLetter, cellWidth);
        }


         */
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


    // Getter עבור selectedRangeProperty
    public ObjectProperty<CellRange> selectedRangeProperty() {
        return selectedRange;
    }

    // פונקציה להגדיר את הטווח הנבחר
    private void setSelectedRange(Coordinate topLeft, Coordinate bottomRight) {
        selectedRange.set(new CellRange(topLeft, bottomRight));  // יוצרים טווח חדש ומעדכנים את ה-Property
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


    public String getSelectedCoordinateOriginalValue()
    {
        CellDto cell = DisplayedSheet.getCell(getSelectedCoordinate());
        if(cell != null)
        {
            return cell.getOriginalValue();
        }
        return ""; // this will
    }


    public int getLastUpdatedVersion()
    {

        CellDto cell = DisplayedSheet.getCell(getSelectedCoordinate());
        if(cell != null)
        {
            return cell.getVersion();
        }

        return 0; // this will
    }





    public void resetSorting() {

        sortedRowOrder = DisplayedSheet.resetSoretedOrder();


    }


    public void loadSheetVersion(int version) //delete after moved!!!!!!!!!!!
    {
        /*
        SheetDto versionDTO = sheetEngine.getVersionDto(version);
        updateSheet(versionDTO);

         */
    }


    public void loadSheetCurrent() //Delete after moved!!!
    {

        //SheetDto sheetDtoCurrent = sheetEngine.getCurrentSheetDTO();
        updateSheet();

    }


    public SheetDto getVersionDto(int version) //delete after move!!!!!!!!!!!!!
    {
        /*
        return sheetEngine.getVersionDto(version);

         */
        return DisplayedSheet;
    }

    public List<Integer> getVersionList()
    {
        //DisplayedSheet.
        //return sheetEngine.getNumChangedCellsInAllVersions();
        return DisplayedSheet.getNumCellChangedHistory();
    }


    public void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight, List<Character> colList) {
        // המרת הטווח לרשימת שורות על פי הסדר הקיים ב-sortedRowOrder
        List<Integer> rowsInRange = getRowsFromCoordinates(topLeft, bottomRight);


        List<Integer> rangeSorted = DisplayedSheet.sortRowsByColumns(rowsInRange, colList);

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




    public Map<String, RangeDto> getRanges()
    {
        return DisplayedSheet.getRanges();
    } // keep track of range versions as well! because changed range effects sheet as well!


    //keep here
    public void highlightFunctionRange(String rangeName)
    {
        BoundariesDto currBoundaries = DisplayedSheet.getRanges().get(rangeName).getBoundaries();
        Coordinate from = CoordinateCache.createCoordinateFromString(currBoundaries.getFrom());
        Coordinate to = CoordinateCache.createCoordinateFromString(currBoundaries.getTo());

        highlightSelectedRangeAccordingToOgRange(from, to); ////// need to update to work with after sorting where cells are scattered!
    }


    //Delete after move!!!!
    public void deleteRange(String rangeName) throws Exception {
        try {
            /*
            sheetEngine.deleteRange(rangeName);

             */
        } catch (Exception e) {
            // Rethrow the exception to allow it to propagate
            throw e;
        }
    }

    //Delete after move!!!!
    public void addRange(String rangeName) throws Exception{
        try {
            /*
            sheetEngine.addRange(rangeName ,coordinateToString(selectedRange.get().getTopLeft()), coordinateToString(selectedRange.get().getBottomRight()));
             */
        } catch (Exception e) {
            // Rethrow the exception to allow it to propagate
            throw e;
        }

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

        return DisplayedSheet.getUniqueValuesInRange(rows,getSelectedColumns());
    }




    // check if exception might be from here!!!
    public void removeRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight) {
        // חיפוש כל השורות שיש להסיר על פי הערך המסומן והעמודה
        List<Integer> rowsToRemove = new ArrayList<>();

        for (int gridRow = topLeft.getRow(); gridRow <= bottomRight.getRow(); gridRow++) {
            // do this more readble!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            int actualRow = lastSortedOrderBeforeFiltering.get(gridRow - 1); // מקבל את השורה המקורית לפי התרגום ב-sortedRowOrder
            Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, convertColumnNameToNumber(columnName));
            CellDto cell = DisplayedSheet.getCell(coordinate);
            if (cell != null && cell.getValue().equals(value)) {
                // עדכון מפת הספירה
                removalCountMap.put(actualRow, removalCountMap.getOrDefault(actualRow, 0) + 1);
                rowsToRemove.add(actualRow);
            }
        }

        // הסרת השורות מרשימת sortedRowOrder
        sortedRowOrder.removeAll(rowsToRemove);

        // עדכון התצוגה שלך, לוודא שהרשימה מעודכנת כראוי ב-UI
        updateSheet();
    }


    public void addRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight) {
        // השגת רשימת השורות המתורגמות לפי הסדר הנוכחי
        List<Integer> rowsInRange = getRowsFromCoordinatesBeforeFiltering(topLeft, bottomRight);

        // חיפוש כל השורות שיש להחזיר לפי הערך המסומן מחדש
        List<Integer> rowsToAdd = new ArrayList<>();

        // מעבר על השורות המתורגמות שהתקבלו
        for (Integer actualRow : rowsInRange) {
            Coordinate coordinate = CoordinateCache.createCoordinate(actualRow, convertColumnNameToNumber(columnName));
            CellDto cell = DisplayedSheet.getCell(coordinate);

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
        updateSheet();
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




    // פונקציה לקבלת רוחב תא מסוים
    public double getCellWidth() {
        String columnName = convertColumnNumberToString(selectedCoordinate.getColumn());
        return columnWidth.getOrDefault(columnName, cellWidth);
    }

    // פונקציה לקבלת גובה תא מסוים
    public double getCellHeight() {
        Integer rowName = (selectedCoordinate.getRow());
        return rowHeight.getOrDefault(rowName, cellHeight);
    }

    // פונקציה לקבלת ממוצע רוחב תאים בטווח
    public double getAverageCellWidth() {
        List<String> columnsInRange = getSelectedColumns();
        double totalWidth = 0;
        for (String columnName : columnsInRange) {
            totalWidth += columnWidth.getOrDefault(columnName, cellWidth);
        }
        return totalWidth / columnsInRange.size();
    }

    // פונקציה לקבלת ממוצע גובה תאים בטווח
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

            // עדכון ColumnConstraints עבור העמודה המסומנת
            if (columnIndex >= 0 && columnIndex < sheetGridPane.getColumnConstraints().size()) {
                ColumnConstraints colConstraints = sheetGridPane.getColumnConstraints().get(columnIndex);
                colConstraints.setPrefWidth(newWidth);
                colConstraints.setMinWidth(newWidth);
                colConstraints.setMaxWidth(newWidth);

                // עדכון המפה columnWidth
                String columnName = convertColumnNumberToString(columnIndex);
                columnWidth.put(columnName, newWidth);
            } else {
                System.out.println("Index out of bounds for column constraints.");
            }
        } else if (selectedRange.get() != null) {
            // עדכון רוחב העמודות בטווח המסומן
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

                    // עדכון המפה columnWidth
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
            int uiRowIndex = selectedCoordinate.getRow(); // אינדקס השורה ב-GridPane (כולל שורת כותרת)
            if (uiRowIndex == 0) {
                System.out.println("Cannot change height of header row.");
                return;
            }

            // התאמה עקב שורת הכותרת
            int gridRowIndex = uiRowIndex;
            int gridRowDataIndex = gridRowIndex - 1; // השורות בנתונים מתחילות מאינדקס 0

            if (gridRowIndex >= 0 && gridRowIndex < sheetGridPane.getRowConstraints().size()) {
                // קבלת מספר השורה האמיתי מתוך sortedRowOrder
                if (gridRowDataIndex >= 0 && gridRowDataIndex < sortedRowOrder.size()) {
                    int actualRowNumber = sortedRowOrder.get(gridRowDataIndex);

                    // עדכון RowConstraints עבור השורה המסומנת
                    RowConstraints rowConstraints = sheetGridPane.getRowConstraints().get(gridRowIndex);
                    rowConstraints.setPrefHeight(newHeight);
                    rowConstraints.setMinHeight(newHeight);
                    rowConstraints.setMaxHeight(newHeight);

                    // עדכון המפה rowHeight
                    rowHeight.put(actualRowNumber, newHeight);
                } else {
                    System.out.println("Index out of bounds for sortedRowOrder.");
                }
            } else {
                System.out.println("Index out of bounds for row constraints.");
            }
        } else if (selectedRange.get() != null) {
            // עדכון גובה השורות בטווח המסומן
            Coordinate topLeft = selectedRange.get().getTopLeft();
            Coordinate bottomRight = selectedRange.get().getBottomRight();

            // קבלת רשימת השורות בפועל בטווח באמצעות הפונקציה getRowsInRange
            List<Integer> rowsToUpdate = getRowsInRange(topLeft, bottomRight);

            // עבור כל מספר שורה בפועל, נאתר את המיקום שלו ב-GridPane
            for (Integer actualRowNumber : rowsToUpdate) {
                int gridRowDataIndex = sortedRowOrder.indexOf(actualRowNumber);
                if (gridRowDataIndex != -1) {
                    int gridRowIndex = gridRowDataIndex + 1; // התאמה עקב שורת הכותרת

                    if (gridRowIndex >= 0 && gridRowIndex < sheetGridPane.getRowConstraints().size()) {
                        // עדכון RowConstraints
                        RowConstraints rowConstraints = sheetGridPane.getRowConstraints().get(gridRowIndex);
                        rowConstraints.setPrefHeight(newHeight);
                        rowConstraints.setMinHeight(newHeight);
                        rowConstraints.setMaxHeight(newHeight);

                        // עדכון המפה rowHeight
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


    // פונקציה לקבלת רשימת שורות בטווח
    private List<Integer> getRowsInRange(Coordinate topLeft, Coordinate bottomRight) {
        List<Integer> rows = new ArrayList<>();
        int startRow = Math.min(topLeft.getRow(), bottomRight.getRow());
        int endRow = Math.max(topLeft.getRow(), bottomRight.getRow());

        for (int gridRowIndex = startRow; gridRowIndex <= endRow; gridRowIndex++) {
            // מתרגמים את אינדקס השורה ב-GridPane למספר שורה מקורי באמצעות sortedRowOrder
            if (gridRowIndex - 1 < sortedRowOrder.size()) {
                int actualRowNumber = sortedRowOrder.get(gridRowIndex - 1);
                rows.add(actualRowNumber);
            }
        }
        return rows;
    }



    // move to viewfinder!
    public void ChangeBackground(String colorHex) {

            // קבלת העמודות והשורות שנבחרו
            List<String> selectedColumns = getSelectedColumns();
            List<Integer> selectedRows = getRowsInRange(startCoordinate, endCoordinate); // מניחים ש- startCoordinate ו- endCoordinate מייצגים את הטווח שנבחר

            // מעבר על כל השורות והעמודות
            for (Integer row : selectedRows) {
                for (String column : selectedColumns) {
                    // יצירת מחרוזת המייצגת את התא, לדוגמה: "A1"
                    String cellReference = column + row;

                    //sheetEngine.setBackgrountColor(cellReference, colorHex);
                }
            }

            updateSheet();
    }




    // move to viewfinder!
    public void ChangeTextColor(String colorHex) {

        // קבלת העמודות והשורות שנבחרו
        List<String> selectedColumns = getSelectedColumns();
        List<Integer> selectedRows = getRowsInRange(startCoordinate, endCoordinate); // מניחים ש- startCoordinate ו- endCoordinate מייצגים את הטווח שנבחר

        // מעבר על כל השורות והעמודות
        for (Integer row : selectedRows) {
            for (String column : selectedColumns) {
                // יצירת מחרוזת המייצגת את התא, לדוגמה: "A1"
                String cellReference = column + row;

                //sheetEngine.setFontColor(cellReference, colorHex);
            }
        }

        updateSheet();
    }






    public Coordinate getDisplayedCellPosition(Coordinate originalCoord) {
        // מיפוי בין אינדקסי שורות מקוריים לאינדקסים מוצגים

        // קבלת האינדקסים המוצגים של השורה והעמודה
        Integer displayedRowIndex = rowIndexMap.get(originalCoord.getRow());
        Integer displayedColumnIndex = originalCoord.getColumn();

        // אם השורה  לא מוצגת, מחזירים null
        if (displayedRowIndex == null) {
            return null;
        }

        // מחזירים את המיקום המוצג של התא
        return CoordinateCache.createCoordinate(displayedRowIndex, displayedColumnIndex);

    }



    private void highlightDepCoordinate(Coordinate curr) {
        Pane overlay = overlayMap.get(curr);
        if (overlay != null) {// הסרת מחלקות CSS קודמות במידת הצורך
            overlay.getStyleClass().removeAll("highlighted-influence", "highlighted-pane", "default-cell");
            // הוספת מחלקת ה-CSS המתאימה
            overlay.getStyleClass().add("highlighted-dependency");}
    }

    private void highlightInfCoordinate(Coordinate curr) {
        Pane overlay = overlayMap.get(curr);
        if (overlay != null) {
            // הסרת מחלקות CSS קודמות במידת הצורך
            overlay.getStyleClass().removeAll("highlighted-dependency", "highlighted-pane", "default-cell");
            // הוספת מחלקת ה-CSS המתאימה
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



    public void ChangeAlignment(String Ali) // move to viewfinder!!!!!!!!!!
    {
        // קבלת העמודות והשורות שנבחרו
        List<String> selectedColumns = getSelectedColumns();
        List<Integer> selectedRows = getRowsInRange(startCoordinate, endCoordinate); // מניחים ש- startCoordinate ו- endCoordinate מייצגים את הטווח שנבחר

        // מעבר על כל השורות והעמודות
        for (Integer row : selectedRows) {
            for (String column : selectedColumns) {
                // יצירת מחרוזת המייצגת את התא, לדוגמה: "A1"
                String cellReference = column + row;

                //sheetEngine.setAlignment(cellReference, Ali);
            }
        }

        updateSheet();
    }


    public void resetStyle() //Move to viewfinder!!!!
    {
        // קבלת העמודות והשורות שנבחרו
        List<String> selectedColumns = getSelectedColumns();
        List<Integer> selectedRows = getRowsInRange(startCoordinate, endCoordinate); // מניחים ש- startCoordinate ו- endCoordinate מייצגים את הטווח שנבחר

        // מעבר על כל השורות והעמודות
        for (Integer row : selectedRows) {
            for (String column : selectedColumns) {
                // יצירת מחרוזת המייצגת את התא, לדוגמה: "A1"
                String cellReference = column + row;

                //sheetEngine.resetStyle(cellReference);
            }
        }

        updateSheet();
    }




    // מתודה להגדרת קריאה בלבד
    public void setReadOnly(boolean readOnly) {
        for (Node node : sheetGridPane.getChildren()) {
            node.setMouseTransparent(readOnly); // מבטל אינטראקציות עכבר
        }
        isReadOnly = true;
    }





    // מתודה להגדרת קריאה בלבד
    public Coordinate actualCellPlacedOnGrid(Coordinate placeOnGrid) {
        return (CoordinateCache.createCoordinate(sortedRowOrder.get(placeOnGrid.getRow() - 1), placeOnGrid.getColumn()));
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



    //sets the dto of sheet to be displayed
    public void setPresentedSheet(SheetDto sheetDto)
    {
        DisplayedSheet = sheetDto;
        updateSheet();
    }


    public int getCurrentSheetVersion()
    {
        return DisplayedSheet.getVersion();
    }


}
