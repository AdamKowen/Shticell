package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class OrExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue leftValue = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);
        System.out.println(leftValue.getCellType() + " " + rightValue.getCellType());
        // Validate argument types
        if (!leftValue.getCellType().equals(CellType.BOOLEAN) || !rightValue.getCellType().equals(CellType.BOOLEAN)) {
            return new EffectiveValueImpl(CellType.STRING, "UNKNOWN");
        }

        boolean result = leftValue.extractValueWithExpectation(Boolean.class) || rightValue.extractValueWithExpectation(Boolean.class);
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
