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


    // שינוי יישור של התאים
    void alignCells(Pos alignment);

    // פעולה לסימון תאים (לדוגמה: לסמן תאים שתלויים אחד בשני)
    void markCellsButtonActionListener(boolean isMarked);

    // פעולה לשינוי צבע התאים
    void toggleCellColor(boolean isSelected);

    // עדכון תוכן של תא על פי קואורדינטה ותוכן חדש
    void updateCellContent(Coordinate coordinate, String content);

    // שינוי רוחב של עמודה מסוימת
    void changeSecondColumnWidth(double width);

    // שינוי גובה של שורה מסוימת
    void changeSecondRowWidth(double width);

    void updateSheet();

    int getLastUpdatedVersion();

    // החזרת הקואורדינטה של התא שנבחר
    Coordinate getSelectedCoordinate();



    // og text of cell
    String getSelectedCoordinateOriginalValue();


    // החזרת ה-Property של התא הנבחר
    ObjectProperty<Label> selectedCellProperty();


    void loadSheetFromFile(String filename) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException;

    void loadSheetVersion(int version);

    void loadSheetCurrent();

    SheetDto getVersionDto(int version);

    void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight, List<Character> colList);

    void resetSorting();


    ObjectProperty<CellRange> selectedRangeProperty();

    Map<String, RangeDto> getRanges();

    void highlightFunctionRange(String rangeName);

    void deleteRange(String rangeName) throws Exception;

    void addRange(String rangeName) throws Exception;

    List<String> getSelectedColumns();

    Map<String, List<String>> getUniqueValuesInRange(Coordinate topLeft, Coordinate bottomRight);


    void removeRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight);

    void addRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight);

    void updateColWidth(double newWidth);

    void updateRowHeight(double newWidth);


    // פונקציה לקבלת רוחב תא מסוים
    double getCellWidth();

    // פונקציה לקבלת גובה תא מסוים
     double getCellHeight();

    // פונקציה לקבלת ממוצע רוחב תאים בטווח
     double getAverageCellWidth();

    // פונקציה לקבלת ממוצע גובה תאים בטווח
     double getAverageCellHeight();

     void ChangeBackground(String colorHex);

    void ChangeTextColor(String colorHex);

    Coordinate getDisplayedCellPosition(Coordinate originalCoord);

    void highlightDependencies();

    void ChangeAlignment(String Ali);

    void resetStyle();

    void setReadOnly(boolean readOnly);

    Coordinate actualCellPlacedOnGrid(Coordinate placeOnGrid);

    void reSelect(String Topleft, String Bottomright)throws Exception;


    //sets the dto of sheet to be displayed
    void setPresentedSheet(SheetDto sheetDto);


    int getCurrentSheetVersion();
}




