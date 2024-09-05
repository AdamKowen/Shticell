package sheet.coordinate.impl;

import sheet.coordinate.api.Coordinate;

public class CoordinateImpl implements Coordinate {
    private int row;
    private int column;

    public CoordinateImpl(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordinateImpl that = (CoordinateImpl) o;

        if (row != that.row) return false;
        return column == that.column;
    }


    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }
}



