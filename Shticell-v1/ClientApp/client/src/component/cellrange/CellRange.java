package component.cellrange;

import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;

public class CellRange {
    private final Coordinate topLeft;
    private final Coordinate bottomRight;

    public CellRange(Coordinate start, Coordinate end) {
        // קביעת הקואורדינטות השמאלית העליונה והימנית התחתונה של הריבוע
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startColumn = Math.min(start.getColumn(), end.getColumn());
        int endColumn = Math.max(start.getColumn(), end.getColumn());

        // שמירה של הקואורדינטות הנכונות בפינות הריבוע
        this.topLeft = CoordinateCache.createCoordinate(startRow, startColumn);
        this.bottomRight = CoordinateCache.createCoordinate(endRow, endColumn);
    }

    public Coordinate getTopLeft() {
        return topLeft;
    }

    public Coordinate getBottomRight() {
        return bottomRight;
    }

    @Override
    public String toString() {
        return "Range: " + topLeft + " to " + bottomRight;
    }
}
