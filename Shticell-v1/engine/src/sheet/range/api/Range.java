package sheet.range.api;

import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.range.boundaries.Boundaries;

import java.util.List;

public interface Range {
    String getName();
    Boundaries getBoundaries();
}

