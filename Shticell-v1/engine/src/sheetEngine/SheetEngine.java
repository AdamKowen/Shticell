package sheetEngine;

import dto.CellDto;
import dto.SheetDto;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.coordinate.api.Coordinate;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface SheetEngine {

    void loadSheetFromXML(String filePath) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException;

    SheetDto getCurrentSheetDTO();

    CellDto getCellDTO(String coordinate);

    void recalculateSheet();

    void updateCellValue(String cell, String newValue);

    List<Integer> getNumChangedCellsInAllVersions();

    SheetDto getVersionDto(int version);

    boolean isSheetLoaded();

    boolean isCoordinateInRange(String str);

    boolean isCellEmpty(String str);

    void deleteRange(String str);
}
