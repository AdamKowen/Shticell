
package sheetEngine;

import dto.CellDto;
import dto.CellDtoImpl;
import dto.SheetDto;
import dto.SheetDtoImpl;
import loader.Loader;
import loader.LoaderImpl;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheet.api.Sheet;
import sheet.api.SheetCalculator;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;
import sheetCalculator.SheetCalculatorImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SheetEngineImpl implements sheetEngine.SheetEngine {

    private Sheet currentSheet;
    private Loader loader;

    public SheetEngineImpl() {
        this.loader = new LoaderImpl(); // loading
    }

    //  XML load with og value in cells
    @Override
    public void loadSheetFromXML(String filePath) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException {
        this.currentSheet = loader.loadSheetFromXML(filePath); //with only og value in!
        recalculateSheet();
    }

    // sheet DTO
    @Override
    public SheetDto getCurrentSheetDTO() {
        return (new SheetDtoImpl(currentSheet));
    }

    // cell dto
    @Override
    public CellDto getCellDTO(String str) {
        Coordinate coordinate =  CoordinateCache.createCoordinateFromString(str);
        if (coordinate == null) {
            throw new IllegalArgumentException("Could not create coordinate from string: " + str);
        }

        Cell currentCell = currentSheet.getCell(coordinate);
        if (currentCell != null)
        {
            return new CellDtoImpl(currentSheet.getCell(coordinate));
        }
        else {
            return null;
        }
    }

    @Override
    public void recalculateSheet() {
        SheetCalculator evaluator = new SheetCalculatorImpl(currentSheet);
        currentSheet.saveVersion();
    }

    @Override
    public void updateCellValue(String cell, String newValue)
    {
        Coordinate coordinate =  CoordinateCache.createCoordinateFromString(cell);

        Cell currCell = currentSheet.getSheet().get(coordinate);

        if(currCell != null)
        {
            String oldValue = currCell.getOriginalValue();
            if (!oldValue.equals(newValue)) //will recalculate only if anything changed
            {
                currCell.setCellOriginalValue(newValue, currentSheet.getVersion()+1);
                recalculateSheet();
            }
        }
        else {
            currCell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), newValue, currentSheet.getVersion()+1);
            currentSheet.setCell(coordinate, currCell);
            recalculateSheet();
        }
    }

    @Override
    public List<Integer> getNumChangedCellsInAllVersions()
    {
        return currentSheet.countChangedCellsInAllVersions();
    }


    @Override
    public SheetDto getVersionDto(int version)
    {
        return currentSheet.getVersionDto(version);
    }

    @Override
    public boolean isSheetLoaded()
    {
        return currentSheet != null;
    }

    @Override
    public boolean isCoordinateInRange(String str) {
        // checking range
        Coordinate coordinate = CoordinateCache.createCoordinateFromString(str);
        if(coordinate == null) {
            throw new IllegalArgumentException("Could not create coordinate from string: " + str);
        }
        return (currentSheet.isCoordinateInRange(coordinate));
    }

    @Override
    public boolean isCellEmpty(String str) {
        // checking if cell empty
        Coordinate coordinate = CoordinateCache.createCoordinateFromString(str);
        return (currentSheet.isCellEmpty(coordinate));
    }


    @Override
    public void deleteRange(String str) {
        currentSheet.removeRange(str);
    }

    @Override
   public Map<String, List<String>> getUniqueValuesInRange(List<Integer> rows, List<String> columns)
    {
        SheetDto sheetDto = getCurrentSheetDTO();
        return sheetDto.getUniqueValuesInRange(rows,columns);
    }


    // פונקציות נוספות לעבודה עם הגיליון
    // public void updateCell(...) { ... }
    // public void saveSheet(...) { ... }
}
