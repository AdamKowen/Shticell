package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class LessExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public LessExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue leftValue = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);


        // Validate argument types
        if ((!leftValue.getCellType().equals(CellType.NUMERIC)) ||
                (!rightValue.getCellType().equals(CellType.NUMERIC))) {
            //throw new IllegalArgumentException("Invalid argument types for LESS function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            return new EffectiveValueImpl(CellType.STRING, "UNKNOWN");
        }

        boolean result = leftValue.extractValueWithExpectation(Double.class) <= rightValue.extractValueWithExpectation(Double.class);
        return new EffectiveValueImpl(CellType.BOOLEAN, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }


    @Override
    public Boolean doesContainRef(){
        return left.doesContainRef() || right.doesContainRef();
    }



    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (right instanceof RefExpression)
        {
            dependencies.add(((RefExpression) right).getRefValue());
        }
        else {
            right.collectDependencies(dependencies);
        }

        if (left instanceof RefExpression)
        {
            dependencies.add(((RefExpression) left).getRefValue());
        }
        else {
            left.collectDependencies(dependencies);
        }
    }
}


