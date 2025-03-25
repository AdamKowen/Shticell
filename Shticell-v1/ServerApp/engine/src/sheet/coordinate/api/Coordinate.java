package sheet.coordinate.api;


public interface Coordinate {
    int getRow();

    int getColumn();

    boolean equals(Object o);

    public int hashCode();
}
