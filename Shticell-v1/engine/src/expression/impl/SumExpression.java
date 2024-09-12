package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class SumExpression implements Expression {
    private final List<Expression> range;

    public SumExpression(List<Expression> range) {
        this.range = range;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        double sum = 0;
        for (Expression expr : range) {
            if(expr.eval(sheet).getCellType()== CellType.NUMERIC)
              sum += expr.eval(sheet).extractValueWithExpectation(Double.class);
        }
        return new EffectiveValueImpl(CellType.NUMERIC, sum);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }

    @Override
    public Boolean doesContainRef() {
        return null;
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies) {

    }
}
