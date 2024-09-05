package sheet.api;

import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;

import java.util.*;

public interface SheetCalculator {


    /**
     * מחשב את סדר החישוב של התאים בגיליון באמצעות DFS
     *
     * @return רשימה של קואורדינטות בסדר החישוב הנכון
     * @throws IllegalArgumentException אם מתגלה מעגל תלות בין תאים
     */
    List<Cell> calculateEvaluationOrder() throws IllegalArgumentException;

}