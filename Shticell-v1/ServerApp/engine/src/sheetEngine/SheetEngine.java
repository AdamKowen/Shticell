package sheetEngine;

import dto.CellDto;
import dto.SheetDto;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.api.Sheet;
import sheet.api.SheetCalculator;
import sheetCalculator.SheetCalculatorImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SheetEngine {

    void loadSheetFromXML(String filePath) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException;

    void loadSheetFromXML(InputStream inputStream) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException;

    SheetDto getCurrentSheetDTO();

    CellDto getCellDTO(String coordinate);

    void recalculateSheet();

    void updateCellValue(String cell, String newValue,String lastUserUpdated) throws Exception;

    SheetDto getVersionDto(int version);

    boolean isSheetLoaded();

    boolean isCoordinateInRange(String str);

    boolean isCellEmpty(String str);

    void deleteRange(String str) throws Exception;

    Map<String, List<String>> getUniqueValuesInRange(List<Integer> rows, List<String> columns);

    void addRange(String str, String from, String to) throws Exception;

    void setBackgrountColor(String cell, String color);
    void setFontColor(String cell, String color);
    void setAlignment(String cell, String Ali);
    void resetStyle(String cell);


    HashMap<String, Sheet> getMyFiles();
    HashMap<String, Sheet> getReaderFiles();
    HashMap<String, Sheet> getWriterFiles();


    void setMyFiles(HashMap<String, Sheet> myFiles);
    void setReaderFiles(HashMap<String, Sheet> readerFiles);
    void setWriterFiles(HashMap<String, Sheet> writerFiles);


    boolean setCurrentSheet(String sheetName);

    int getCurrentSheetVersion();

    void updateCellsStyle(List<String> columns, List<Integer> rows, String styleType, String styleValue);

    public void updateCellBasedOnSlider(String cellID, String value) throws Exception;

    SheetDto getTemporarySheetDTO();


    void recalculateTempSheet();

    void updateCellValueTempSheet(String cell, String newValue) throws Exception;

    String getCurrentSheetName();

    // adds sheet to read
    void addSheetToRead(Sheet passSheet);

    // adds sheet to write
    void addSheetToWrite(Sheet passSheet);

    void passSheetPermission(String passSheetName, SheetEngine usersEngine, String permission);

    void resetTempSheet();
}
