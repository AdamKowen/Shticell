package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class PercentExpression implements Expression {
    private final Expression part;
    private final Expression whole;

    public PercentExpression(Expression part, Expression whole) {
        this.part = part;
        this.whole = whole;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        double partValue = part.eval(sheet).extractValueWithExpectation(Double.class);
        double wholeValue = whole.eval(sheet).extractValueWithExpectation(Double.class);

        // Validate argument types
        if ((!part.eval(sheet).getCellType().equals(CellType.NUMERIC) ) ||
                (!whole.eval(sheet).getCellType().equals(CellType.NUMERIC) )) {
            //throw new IllegalArgumentException("Invalid argument types for PERCENT function. Expected NUMERIC, but got " + part.getFunctionResultType() + " and " + whole.getFunctionResultType());
            return new EffectiveValueImpl(CellType.STRING, "NaN");
        }

        double result = (partValue * wholeValue) / 100;
        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
    @Override
    public Boolean doesContainRef(){
        return part.doesContainRef() || whole.doesContainRef();
    }



    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (whole instanceof RefExpression)
        {
            dependencies.add(((RefExpression) whole).getRefValue());
        }
        else {
            whole.collectDependencies(dependencies);
        }

        if (part instanceof RefExpression)
        {
            dependencies.add(((RefExpression) part).getRefValue());
        }
        else {
            part.collectDependencies(dependencies);
        }
    }
}
