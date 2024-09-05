package loader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;


import loader.generated.STLSheet; // מחלקה שנוצרה על ידי JAXB
import sheet.api.Sheet;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateImpl;
import sheet.impl.SheetImpl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class LoaderImpl implements Loader {

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

            // בדיקת תקינות השימוש בפונקציות
            if (cell.getSTLOriginalValue().contains("REF")) { ////////////// in another place!
                // כאן תוכל להוסיף בדיקות נוספות על תקינות פונקציות ושימוש בתאים מתאימים
                // לדוגמה, בדיקת התאמת סוגי התאים לפונקציות מסוימות
            }
        }
    }

    private Sheet convertToSheet(STLSheet stlSheet) {
        String name = stlSheet.getName();
        int numRows = stlSheet.getSTLLayout().getRows();
        int numColumns = stlSheet.getSTLLayout().getColumns();
        int columnUnits = stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();
        int rowUnits = stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();

        Map<Coordinate, Cell> cellsInSheet = new HashMap<>();

        Sheet newSheet = new SheetImpl(name, numColumns, numRows, columnUnits, rowUnits, cellsInSheet);

        for (loader.generated.STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            int row = stlCell.getRow();
            int column = stlCell.getColumn().charAt(0) - 'A' + 1; // המרה מ-אות למספר

            String originalValue = stlCell.getSTLOriginalValue();
            int cellVersion = 1; // בגרסה 1 לצורך הפשטות

            Coordinate coordinate = new CoordinateImpl(row, column);
            Cell cell = new CellImpl(row, column, originalValue, cellVersion);


            /*
            // מחשב את הערך האפקטיבי של התא
            cell.calculateEffectiveValue(newSheet); // העבר את ה-Sheet המתאים במידת הצורך
            moving this to calculator!!!!!
             */

            cellsInSheet.put(coordinate, cell);
        }

        // החזרת אובייקט Sheet מלא בנתונים
        return newSheet;
    }

    /*
    private Sheet convertToSheet(STLSheet stlSheet) {
        String name = stlSheet.getName();
        int numRows = stlSheet.getSTLLayout().getRows();
        int numColumns = stlSheet.getSTLLayout().getColumns();
        int columnUnits = stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();
        int rowUnits = stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();

        Map<Coordinate, Cell> cellsInSheet = new HashMap<>();

        for (loader.generated.STLCell stlCell : stlSheet.getSTLCells().getSTLCell()) {
            int row = stlCell.getRow();
            int column = stlCell.getColumn().charAt(0) - 'A' + 1; // המרה מ-אות למספר

            String originalValue = stlCell.getSTLOriginalValue();
            int cellVersion = 1; // בגרסה 1 לצורך הפשטות

            Coordinate coordinate = new CoordinateImpl(row, column);
            Cell cell = new CellImpl(row, column, originalValue, cellVersion);
            cellsInSheet.put(coordinate, cell);
        }

        // החזרת אובייקט Sheet מלא בנתונים
        return new SheetImpl(name, numColumns, numRows, columnUnits, rowUnits, cellsInSheet);
    }

     */
}



/*
package loader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sheet.api.Sheet;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateImpl;
import sheet.impl.SheetImpl;

public class LoaderImpl implements Loader {

    public Sheet loadSheetFromXML(String filePath, SheetReadActions sheet) throws SheetLoadingException, ParserConfigurationException, SAXException, IOException {
        // בדיקות תקינות הקובץ
        if (!filePath.endsWith(".xml")) {
            throw new SheetLoadingException("The file is not an XML file. Please provide a valid XML file.");
        }

        File xmlFile = new File(filePath);
        if (!xmlFile.exists() || !xmlFile.isFile()) {
            throw new SheetLoadingException("The file does not exist or is not a valid file. Please check the path and try again.");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);

        // אימות התוכן של הקובץ
        validateSheet(document);

        // יצירת אובייקט Sheet מהנתונים
        return loadSheet(document);

        /*catch (Exception e) {
            throw new SheetLoadingException("An error occurred while loading the XML file.", e); } // move to ui!
    }


    public void validateSheet(Document document) throws SheetLoadingException {
        Element layoutElement = (Element) document.getElementsByTagName("STL-Layout").item(0);
        int numRows = Integer.parseInt(layoutElement.getAttribute("rows"));
        int numColumns = Integer.parseInt(layoutElement.getAttribute("columns"));

        if (numRows < 1 || numRows > 50 || numColumns < 1 || numColumns > 20) {
            throw new SheetLoadingException("The sheet size is invalid. Rows must be between 1 and 50, columns between 1 and 20.");
        }

        NodeList cells = document.getElementsByTagName("STL-Cell");
        for (int i = 0; i < cells.getLength(); i++) {
            Element cell = (Element) cells.item(i);
            int row = Integer.parseInt(cell.getAttribute("row"));
            String columnStr = cell.getAttribute("column");
            int column = columnStr.charAt(0) - 'A' + 1;

            if (row < 1 || row > numRows || column < 1 || column > numColumns) {
                throw new SheetLoadingException("Cell found out of sheet boundaries at row: " + row + ", column: " + columnStr);
            }
        }
    }


    public Sheet loadSheet(Document document) {
        String name = "Sheet1"; // ניתן להתאים לפי הצורך
        int version = 1; // ניתן לשנות בהתאם לצורך

        Element layoutElement = (Element) document.getElementsByTagName("STL-Layout").item(0);
        int numRows = Integer.parseInt(layoutElement.getAttribute("rows"));
        int numColumns = Integer.parseInt(layoutElement.getAttribute("columns"));

        Element sizeElement = (Element) document.getElementsByTagName("STL-Size").item(0);
        int columnUnits = Integer.parseInt(sizeElement.getAttribute("column-width-units"));
        int rowUnits = Integer.parseInt(sizeElement.getAttribute("rows-height-units"));

        Map<Coordinate, Cell> cellsInSheet = new HashMap<>();

        Sheet currSheet = new SheetImpl(name, numColumns, numRows, columnUnits, rowUnits, cellsInSheet);

        NodeList cellNodes = document.getElementsByTagName("STL-Cell");
        for (int i = 0; i < cellNodes.getLength(); i++) {
            Element cellElement = (Element) cellNodes.item(i);
            int row = Integer.parseInt(cellElement.getAttribute("row"));
            String columnStr = cellElement.getAttribute("column");
            int column = columnStr.charAt(0) - 'A' + 1;

            String originalValue = cellElement.getElementsByTagName("STL-Original-Value").item(0).getTextContent();
            int cellVersion = 1; // בגרסה 1 לצורך הפשטות

            Coordinate coordinate = new CoordinateImpl(row, column);
            Cell cell = new CellImpl(row, column, originalValue, cellVersion);
            cell.calculateEffectiveValue(currSheet);
            cellsInSheet.put(coordinate, cell);
        }

        // שימוש בקונסטרקטור החדש של SheetImpl
        return currSheet;
    }
}
*/
