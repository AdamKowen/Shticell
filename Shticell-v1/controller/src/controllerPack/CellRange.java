package controllerPack;

import sheet.coordinate.api.Coordinate;

public class CellRange {
    private final Coordinate topLeft;
    private final Coordinate bottomRight;

    public CellRange(Coordinate topLeft, Coordinate bottomRight) {
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
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
