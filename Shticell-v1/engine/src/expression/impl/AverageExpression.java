package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class AverageExpression implements Expression {
    private final List<Expression> range;

    public AverageExpression(List<Expression> range) {
        this.range = range;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        double sum = 0;
        int count = 0;
        for (Expression expr : range) {
            if (expr.getFunctionResultType()== CellType.NUMERIC) {
                sum += expr.eval(sheet).extractValueWithExpectation(Double.class);
                count++;
            }
        }
        if (count == 0) {
            //throw new IllegalArgumentException("No numeric cells found in the range.");
            return new EffectiveValueImpl(CellType.NUMERIC, 0);
        }

        return new EffectiveValueImpl(CellType.NUMERIC, sum / count);
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

