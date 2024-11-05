package loader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;


import loader.generated.STLSheet;
import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateImpl;
import sheet.impl.SheetImpl;
import sheet.range.api.Range;
import sheet.range.boundaries.Boundaries;
import sheet.range.impl.RangeImpl;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class LoaderImpl implements Loader {

    @Override
    public Sheet loadSheetFromXML(InputStream inputStream) throws SheetLoadingException {
        try {
            // שימוש ב-JAXB כדי לפרש את קובץ ה-XML מתוך InputStream
            JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            STLSheet stlSheet = (STLSheet) unmarshaller.unmarshal(inputStream);

            // אימות התוכן של הקובץ
            validateSheet(stlSheet);

            return convertToSheet(stlSheet);
        } catch (JAXBException e) {
            throw new SheetLoadingException("An error occurred while loading the XML file from InputStream.", e);
        }
    }



    public Sheet loadSheetFromXML(String filePath) throws SheetLoadingException {
        // בדיקות תקינות הקובץ
        if (!filePath.endsWith(".xml")) {
            throw new SheetLoadingException("The file is not an XML file. Please provide a valid XML file.");
        }

        File xmlFile = new File(filePath);
        if (!xmlFile.exists() || !xmlFile.isFile()) {
            throw new SheetLoadingException("The file does not exist or is not a valid file. Please check the path and try again.");
        }

        try {
            // שימוש ב-JAXB כדי לפרש את קובץ ה-XML
            JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            STLSheet stlSheet = (STLSheet) unmarshaller.unmarshal(xmlFile);

            // אימות התוכן של הקובץ
            validateSheet(stlSheet);

            return convertToSheet(stlSheet);
        } catch (JAXBException e) {
            throw new SheetLoadingException("An error occurred while loading the XML file.", e);
        }
    }

    private void validateSheet(STLSheet stlSheet) throws SheetLoadingException {
        int numRows = stlSheet.getSTLLayout().getRows();
        int numColumns = stlSheet.getSTLLayout().getColumns();

        // בדיקת גודל הגיליון
        if (numRows < 1 || numRows > 50 || numColumns < 1 || numColumns > 20) {
            throw new SheetLoadingException("The sheet size is invalid. Rows must be between 1 and 50, columns between 1 and 20.");
        }

        // בדיקת מיקום התאים בגבולות הגיליון
        List<loader.generated.STLCell> cells = stlSheet.getSTLCells().getSTLCell();
        for (loader.generated.STLCell cell : cells) {
            int row = cell.getRow();
            int column = cell.getColumn().charAt(0) - 'A' + 1;
            if (row < 1 || row > numRows || column < 1 || column > numColumns) {
                throw new SheetLoadingException("Cell found out of sheet boundaries at row: " + row + ", column: " + cell.getColumn());
            }
        }
    }

    private Sheet convertToSheet(STLSheet stlSheet) {
        String name = stlSheet.getName();
        int numRows = stlSheet.getSTLLayout().getRows();
        int numColumns = stlSheet.getSTLLayout().getColumns();
        int columnUnits = stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();
        int rowUnits = stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        //     HashMap<String ,Range>= stlSheet.getSTLRanges().getSTLRange()
        Map<Coordinate, Cell> cellsInSheet = new HashMap<>();
        Map<String, Range> rangesInSheet = new HashMap<>();

        Sheet newSheet = new SheetImpl(name, numColumns, numRows, columnUnits, rowUnits, cellsInSheet,rangesInSheet);

        for (loader.generated.STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            int row = stlCell.getRow();
            int column = stlCell.getColumn().charAt(0) - 'A' + 1; // המרה מ-אות למספר

            String originalValue = stlCell.getSTLOriginalValue();
            int cellVersion = 1; // בגרסה 1 לצורך הפשטות

            Coordinate coordinate = new CoordinateImpl(row, column);
            Cell cell = new CellImpl(row, column, originalValue, cellVersion);

            cellsInSheet.put(coordinate, cell);
        }


        for (loader.generated.STLRange stlRange : stlSheet.getSTLRanges().getSTLRange()) {
            if(stlRange!=null) {
                String from = stlRange.getSTLBoundaries().getFrom();
                String to = stlRange.getSTLBoundaries().getTo();
                Boundaries newBoundaries = new Boundaries(from, to);

                String rangeName = stlRange.getName();

                Range resRange = new RangeImpl(newBoundaries, rangeName);
                rangesInSheet.put(rangeName, resRange);
            }

        }

        // החזרת אובייקט Sheet מלא בנתונים
        return newSheet;

    }

}
