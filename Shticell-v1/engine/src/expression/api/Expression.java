package expression.api;


import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.coordinate.api.Coordinate;

import java.util.List;
import java.util.Set;

public interface Expression {
    EffectiveValue eval(SheetReadActions sheet);

    CellType getFunctionResultType();

    Boolean doesContainRef();

    void collectDependencies(List<Coordinate> dependencies);

}