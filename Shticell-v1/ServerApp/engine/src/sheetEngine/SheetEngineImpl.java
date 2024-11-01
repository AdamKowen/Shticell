
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
import sheet.range.boundaries.Boundaries;
import sheet.range.impl.RangeImpl;
import sheetCalculator.SheetCalculatorImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetEngineImpl implements sheetEngine.SheetEngine {

    private Sheet currentSheet;
    private Sheet temporarySheet = null;
    private Loader loader;
    private HashMap<String, Sheet> MyFiles;
    private HashMap<String, Sheet> readerFiles;
    private HashMap<String, Sheet> writerFiles;





    public SheetEngineImpl() {
        this.loader = new LoaderImpl();
        this.MyFiles = new HashMap<>();
        this.readerFiles = new HashMap<>();
        this.writerFiles = new HashMap<>();
        // this.nameofowner = name;
    }


    //  XML load with og value in cells
    @Override
    public void loadSheetFromXML(String filePath) throws ParserConfigurationException, IOException, SheetLoadingException, SAXException {
        currentSheet = loader.loadSheetFromXML(filePath); //with only og value in!
        MyFiles.put(currentSheet.getName(), currentSheet);
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
    public void updateCellValue(String cell, String newValue) throws Exception {
        Coordinate coordinate = CoordinateCache.createCoordinateFromString(cell);

        Cell currCell = currentSheet.getSheet().get(coordinate);

        if (currCell != null) {
            String oldValue = currCell.getOriginalValue();
            if (!oldValue.equals(newValue)) { // Recalculate only if the value changed
                currCell.setCellOriginalValue(newValue, currentSheet.getVersion() + 1);
                recalculateSheet();
            }
        } else {
            // If the cell doesn't exist, try to create a new one
            try {
                currCell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), newValue, currentSheet.getVersion() + 1);
                currentSheet.setCell(coordinate, currCell);
                recalculateSheet();
            } catch (Exception e) {
                // אם יצירת התא או הוספתו נכשלת, נזרוק שגיאה למעלה
                throw new Exception("Failed to update or create cell at coordinate: " + cell, e);
            }
        }
    }

/*
    @Override
    public List<Integer> getNumChangedCellsInAllVersions()
    {
        return currentSheet.countChangedCellsInAllVersions();
    }

*/


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
    public void deleteRange(String str) throws Exception {
        try {
            currentSheet.removeRange(str);
        } catch (Exception e) {
            // העברת החריגה הלאה
            throw e;
        }
    }

    @Override
    public void addRange(String str, String from, String to) throws Exception {
        try {
            currentSheet.addRange(str, new RangeImpl(new Boundaries(from, to), str));
        } catch (Exception e) {
            // העברת החריגה הלאה
            throw e;
        }
    }


    @Override
   public Map<String, List<String>> getUniqueValuesInRange(List<Integer> rows, List<String> columns)
    {
        SheetDto sheetDto = getCurrentSheetDTO();
        return sheetDto.getUniqueValuesInRange(rows,columns);
    }

    public void setBackgrountColor(String cell, String color)
    {
        Coordinate coordinate =  CoordinateCache.createCoordinateFromString(cell);
        if (coordinate == null) {
            throw new IllegalArgumentException("Could not create coordinate from string: " + cell);
        }

        Cell currentCell = currentSheet.getCell(coordinate);
        if (currentCell != null)
        {
            currentSheet.getCell(coordinate).getStyle().setBackgroundColor(color);
        }
    }


    public void setFontColor(String cell, String color)
    {
        Coordinate coordinate =  CoordinateCache.createCoordinateFromString(cell);
        if (coordinate == null) {
            throw new IllegalArgumentException("Could not create coordinate from string: " + cell);
        }

        Cell currentCell = currentSheet.getCell(coordinate);
        if (currentCell != null)
        {
            currentSheet.getCell(coordinate).getStyle().setTextColor(color);
        }
    }


    public void setAlignment(String cell, String Ali)
    {
        Coordinate coordinate =  CoordinateCache.createCoordinateFromString(cell);
        if (coordinate == null) {
            throw new IllegalArgumentException("Could not create coordinate from string: " + cell);
        }

        Cell currentCell = currentSheet.getCell(coordinate);
        if (currentCell != null)
        {
            currentSheet.getCell(coordinate).getStyle().setAlignment(Ali);
        }
    }


    public void resetStyle(String cell)
    {
        Coordinate coordinate =  CoordinateCache.createCoordinateFromString(cell);
        if (coordinate == null) {
            throw new IllegalArgumentException("Could not create coordinate from string: " + cell);
        }

        Cell currentCell = currentSheet.getCell(coordinate);
        if (currentCell != null)
        {
            currentSheet.getCell(coordinate).getStyle().setToDefault();
        }
    }

    public boolean isEmptyCell(Cell cell){
        return cell == null || cell.getOriginalValue().isBlank();
    }

    public HashMap<String, Sheet> getMyFiles() {
        return MyFiles;
    }
    public HashMap<String, Sheet> getReaderFiles() {
        return readerFiles;
    }
    public HashMap<String, Sheet> getWriterFiles() {
        return writerFiles;
    }

    public void setMyFiles(HashMap<String, Sheet> myFiles) {
        MyFiles = myFiles;
    }
    public void setReaderFiles(HashMap<String, Sheet> readerFiles) {
        this.readerFiles = readerFiles;
    }
    public void setWriterFiles(HashMap<String, Sheet> writerFiles) {
        this.writerFiles = writerFiles;
    }


    public boolean setCurrentSheet(String sheetName) {
        Sheet selectedSheet = null;

        // חיפוש בגיליונות שלך
        if (MyFiles.containsKey(sheetName)) {
            selectedSheet = MyFiles.get(sheetName);
        }
        // חיפוש בגיליונות שאתה רק קורא
        else if (readerFiles.containsKey(sheetName)) {
            selectedSheet = readerFiles.get(sheetName);
        }
        // חיפוש בגיליונות שאתה יכול לערוך
        else if (writerFiles.containsKey(sheetName)) {
            selectedSheet = writerFiles.get(sheetName);
        }

        // אם לא נמצא הגיליון - החזר false
        if (selectedSheet == null) {
            return false;
        }

        // הגדרת הגיליון הנוכחי
        this.currentSheet = selectedSheet;
        return true;
    }



    @Override
    public int getCurrentSheetVersion()
    {
        return currentSheet.getVersion();
    }



    public void updateCellsStyle(List<String> columns, List<Integer> rows, String styleType, String styleValue) {

        int newVersion = currentSheet.getVersion() + 1; // מוודא שהעדכון נחשב כגרסה אחת

        for (int row : rows) {
            for (String column : columns) {
                String cellReference = column + row;
                Coordinate coordinate = CoordinateCache.createCoordinateFromString(cellReference);
                Cell currentCell = currentSheet.getCell(coordinate);

                if (currentCell != null) {
                    switch (styleType) {
                        case "backgroundColor":
                            currentCell.getStyle().setBackgroundColor(styleValue);
                            break;
                        case "textColor":
                            currentCell.getStyle().setTextColor(styleValue);
                            break;
                        case "alignment":
                            currentCell.getStyle().setAlignment(styleValue);
                            break;
                        case "reset":
                            currentCell.getStyle().setToDefault();
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported style type: " + styleType);
                    }

                    currentCell.setVersion(newVersion);
                }
            }
        }

        currentSheet.saveVersion(); // שמירת הגרסה החדשה
    }



    public void createTemporarySheet() {
        if (temporarySheet == null) {
            temporarySheet = deepCopy(currentSheet);
        }
    }

    public void resetTemporarySheet() {
        temporarySheet = null;
    }

    private Sheet deepCopy(Sheet sheet) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(sheet);
            out.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (Sheet) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateCellBasedOnSlider(String cellID, String value) throws Exception {
        if (temporarySheet == null) {
            createTemporarySheet(); // יצירת העתק אם עוד לא קיים
        }
        updateCellValueTempSheet(cellID, value); // זריקת Exception אם מתרחשת שגיאה
    }



    public SheetDto getTemporarySheetDTO() {
        if (temporarySheet == null) {
            return getCurrentSheetDTO(); // במקרה שאין גיליון זמני, מחזיר את הגיליון האמיתי
        }
        temporarySheet.initializaEmptyLists();
        return new SheetDtoImpl(temporarySheet); // יצירת SheetDto עבור הגיליון הזמני
    }


    @Override
    public void recalculateTempSheet() {
        SheetCalculator evaluator = new SheetCalculatorImpl(temporarySheet);
        //temporarySheet.saveVersion();
    }



    @Override
    public void updateCellValueTempSheet(String cell, String newValue) throws Exception {
        Coordinate coordinate = CoordinateCache.createCoordinateFromString(cell);

        Cell currCell = temporarySheet.getSheet().get(coordinate);

        if (currCell != null) {
            String oldValue = currCell.getOriginalValue();
            if (!oldValue.equals(newValue)) { // Recalculate only if the value changed
                currCell.setCellOriginalValue(newValue, temporarySheet.getVersion() + 1);
                recalculateTempSheet();
            }
        } else {
            // If the cell doesn't exist, try to create a new one
            try {
                currCell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), newValue, temporarySheet.getVersion() + 1);
                temporarySheet.setCell(coordinate, currCell);
                recalculateTempSheet();
            } catch (Exception e) {
                // אם יצירת התא או הוספתו נכשלת, נזרוק שגיאה למעלה
                throw new Exception("Failed to update or create cell at coordinate: " + cell, e);
            }
        }
    }


    public String getCurrentSheetName()
    {
        return currentSheet.getName();
    }
}
