package controllerPack;

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

    void updateSheet(SheetDto sheetDto);


    // החזרת הקואורדינטה של התא שנבחר
    Coordinate getSelectedCoordinate();



    // og text of cell
    String getSelectedCoordinateOriginalValue();


    // החזרת ה-Property של התא הנבחר
    ObjectProperty<Label> selectedCellProperty();


    void loadSheetFromFile(String filename) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException;


    List<Integer> getVersionList();

    void loadSheetVersion(int version);

    void loadSheetCurrent();

    SheetDto getVersionDto(int version);

    void sortRowsInRange(Coordinate topLeft, Coordinate bottomRight, List<Character> colList);

    void resetSorting();


    ObjectProperty<CellRange> selectedRangeProperty();

    Map<String, RangeDto> getRanges();

    void highlightFunctionRange(String rangeName);

    boolean deleteRange(String rangeName);

    List<String> getSelectedColumns();

    Map<String, List<String>> getUniqueValuesInRange(Coordinate topLeft, Coordinate bottomRight);


    void removeRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight);

    void addRowsForValue(String columnName, String value, Coordinate topLeft, Coordinate bottomRight);

    void updateRowWidth(double newWidth);
}

