package sheet.coordinate.impl;

import sheet.coordinate.api.Coordinate;

import java.io.Serializable;

public class CoordinateImpl implements Coordinate, Serializable {
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

    @Override
    public String toString() {
        // converts letters
        String columnLetter = convertColumnNumberToLetter(column);

        // returns the right format
        return columnLetter + (row);
    }

    // col num to letter
    private String convertColumnNumberToLetter(int column) {
        StringBuilder columnName = new StringBuilder();
        while (column > 0) {
            column--; // adjusting index to metric
            columnName.insert(0, (char) ('A' + (column % 26)));
            column = column / 26;
        }
        return columnName.toString();
    }
}



