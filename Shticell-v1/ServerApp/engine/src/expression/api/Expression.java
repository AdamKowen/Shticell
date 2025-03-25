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

    // checks if contains reference to another cell
    Boolean doesContainRef();

    // collects dependencies in expression if detects an instance of RefExpression
    void collectDependencies(List<Coordinate> dependencies);

}