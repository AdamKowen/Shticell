package sheet.coordinate.impl;
import sheet.coordinate.api.Coordinate;
import java.util.HashMap;
import java.util.Map;


public class CoordinateCache {
    private static Map<String, Coordinate> cachedCoordinates = new HashMap<>();

    public static Coordinate createCoordinate(int row, int column) {
        String key = row + ":" + column;

        if (cachedCoordinates.containsKey(key)) {
            return cachedCoordinates.get(key);
        }

        CoordinateImpl coordinate = new CoordinateImpl(row, column);
        cachedCoordinates.put(key, coordinate);

        return coordinate;
    }


    public static Coordinate createCoordinateFromString(String trim) {
        try {
            // חילוץ מספרים ואותיות מהקלט
            String columnPart = trim.replaceAll("[^A-Z]", ""); // מוצא את כל האותיות
            String rowPart = trim.replaceAll("[^0-9]", "");    // מוצא את כל המספרים

            // בדיקה אם הקלט מכיל אותיות ומספרים
            if (columnPart.isEmpty() || rowPart.isEmpty()) {
                return null; // החזר null אם הקלט לא תקין
            }

            // המרת האות לעמודה, לדוגמה: A=1, B=2, וכו'
            int column = columnPart.charAt(0) - 'A' + 1;

            // המרת מחרוזת השורה למספר
            int row = Integer.parseInt(rowPart);

            // שימוש במאגרת ליצירת קואורדינטה
            return createCoordinate(row, column);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return null;
        }
    }


    /*
    public static Coordinate createCoordinateFromString(String trim) {
        try {
            String[] parts = trim.split(":");
            return createCoordinate(Integer.parseInt(parts[0]) - 'A', Integer.parseInt(parts[1])); //change from aviad check!!!
        } catch (NumberFormatException e) {
            return null;
        }
    }

     */

}

