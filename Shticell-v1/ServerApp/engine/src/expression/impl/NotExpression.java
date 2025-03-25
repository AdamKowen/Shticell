package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class NotExpression implements Expression {

    private final Expression expr;

    public NotExpression(Expression expr) {
        this.expr = expr;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue value = expr.eval(sheet);
        // Validate argument type
        if (!value.getCellType().equals(CellType.BOOLEAN)) {
            // throw new IllegalArgumentException("Invalid argument type for NOT function. Expected BOOLEAN, but got " + expr.getFunctionResultType());
            return new EffectiveValueImpl(CellType.STRING, "UNKNOWN");
        }

        boolean result = !value.extractValueWithExpectation(Boolean.class);
        return new EffectiveValueImpl(CellType.BOOLEAN, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }

    @Override
    public Boolean doesContainRef(){
        return expr.doesContainRef();
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (expr instanceof RefExpression)
        {
            dependencies.add(((RefExpression) expr).getRefValue());
        }
        else {
            expr.collectDependencies(dependencies);
        }


    }
}



