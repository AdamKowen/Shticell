package controllerPack;

import dto.SheetDto;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.coordinate.api.Coordinate;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

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
}

