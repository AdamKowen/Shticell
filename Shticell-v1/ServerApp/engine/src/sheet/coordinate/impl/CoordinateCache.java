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
            // finds numbers and letters
            String columnPart = trim.replaceAll("[^A-Z]", ""); // finds all letters
            String rowPart = trim.replaceAll("[^0-9]", "");    // finds all numbers

            // checks if contains both letters and numbers
            if (columnPart.isEmpty() || rowPart.isEmpty()) {
                return null; // return null if not valid
            }

            // converts to num from letter
            int column = columnPart.charAt(0) - 'A' + 1;

            // turns the string num to int
            int row = Integer.parseInt(rowPart);

            // using cache to use existing coordinates
            return createCoordinate(row, column);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return null;
        }
    }

}

