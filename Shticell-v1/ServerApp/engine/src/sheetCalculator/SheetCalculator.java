package sheet.api;

import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;

import java.util.*;

public interface SheetCalculator {


    /**
     * calculates the order for calculation with DFS
     *
     * @return list of coordinated in order of calculation
     * @throws IllegalArgumentException if a circle is detected
     */
    List<Cell> calculateEvaluationOrder() throws IllegalArgumentException;

}