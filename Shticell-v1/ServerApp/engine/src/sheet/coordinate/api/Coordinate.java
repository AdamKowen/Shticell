package sheet.coordinate.api;

import sheet.coordinate.impl.CoordinateImpl;

public interface Coordinate {
    int getRow();

    int getColumn();

    boolean equals(Object o);

    public int hashCode();
}
