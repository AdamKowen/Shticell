package component.cellrange;

import sheet.coordinate.api.Coordinate;
import sheet.coordinate.impl.CoordinateCache;

public class CellRange {
    private final Coordinate topLeft;
    private final Coordinate bottomRight;

    public CellRange(Coordinate start, Coordinate end) {
        // sets the coordinates for a selected square range
        int startRow = Math.min(start.getRow(), end.getRow());
        int endRow = Math.max(start.getRow(), end.getRow());
        int startColumn = Math.min(start.getColumn(), end.getColumn());
        int endColumn = Math.max(start.getColumn(), end.getColumn());

        // saves the top left and bottom right
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
