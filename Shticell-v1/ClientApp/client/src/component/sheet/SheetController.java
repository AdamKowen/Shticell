package component.sheet;

import component.cellrange.CellRange;
import dto.CellDto;
import dto.RangeDto;
import dto.SheetDto;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
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
import java.util.Map;

public interface SheetController {


    // Refresh a sheet update
    void updateSheet();

    // Get the last updated version number of the sheet
    int getLastUpdatedVersion();

    // Get the coordinate of the currently selected cell
    Coordinate getSelectedCoordinate();

    // Get the original (raw) text of the selected cell
    String getSelectedCoordinateOriginalValue();

    // Property for observing or binding the currently selected cell Label
    ObjectProperty<Label> selectedCellProperty();

    // Load the latest/current version of the sheet
    void loadSheetCurrent();

    // Sort rows in a given rectangular range using the specified column order
    void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight, List<Character> colList);

    // Cancel all sorting and revert to original row order
    void resetSorting();

    // Property for the currently selected cell range (e.g. for filtering or styling)
    ObjectProperty<CellRange> selectedRangeProperty();

    // Get all defined named ranges on the sheet
    Map<String, RangeDto> getRanges();

    // Highlight a range associated with a function, by name
    void highlightFunctionRange(String rangeName);

    // Get list of columns currently selected
    List<String> getSelectedColumns();

    // Get a map of unique values for each column within a given range
    Map<String, List<String>> getUniqueValuesInRange(Coordinate topLeft, Coordinate bottomRight);

    // Remove rows that contain a specific value in a specific column within the given range
    void removeRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight);

    // Re-add rows that were previously filtered out
    void addRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight);

    // Update the width of all columns
    void updateColWidth(double newWidth);

    // Update the height of all rows
    void updateRowHeight(double newWidth);

    // Get the width of a single cell
    double getCellWidth();

    // Get the height of a single cell
    double getCellHeight();

    // Get the average width of cells in the current range or sheet
    double getAverageCellWidth();

    // Get the average height of cells in the current range or sheet
    double getAverageCellHeight();

    // Get the displayed grid position of a cell by its original coordinate
    Coordinate getDisplayedCellPosition(Coordinate originalCoord);

    // Highlight dependencies of the currently selected cell (e.g. formula references)
    void highlightDependencies();

    // Set read-only mode for the sheet (disable editing)
    void setReadOnly(boolean readOnly);

    // Translate a displayed grid coordinate to the actual underlying data coordinate
    Coordinate actualCellPlacedOnGrid(Coordinate placeOnGrid);

    // Reselect a range of cells given by their top-left and bottom-right string representation
    void reSelect(String Topleft, String Bottomright) throws Exception;

    // Set the sheet to be presented in the UI using a SheetDto
    void setPresentedSheet(SheetDto sheetDto);

    // Get the version number of the currently displayed sheet
    int getCurrentSheetVersion();

    // Get a list of all available versions of the sheet
    List<Integer> getVersionList();

    // Get a list of row indices currently selected (for sorting, styling, etc.)
    List<Integer> getSelectedRows();

    // Get the string representation of the current selection's top-left cell
    String getTopLeft();

    // Get the string representation of the current selection's bottom-right cell
    String getBottomRight();

    // Get the last cell that was updated by the user (as string coordinate)
    String getLastUserUpdatedCell();

    // Get the displayed value of a cell given its string coordinate (e.g. "A1")
    String getCellValue(String cell);

}
