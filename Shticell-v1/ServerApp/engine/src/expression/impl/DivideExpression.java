package expression.impl;
import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;


public class DivideExpression implements Expression {

    private Expression left;
    private Expression right;

    public DivideExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue leftValue = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);

        if (!leftValue.getCellType().equals(CellType.NUMERIC) || !rightValue.getCellType().equals(CellType.NUMERIC)) {
            return new EffectiveValueImpl(CellType.STRING, "NaN");
        }

        if(rightValue.extractValueWithExpectation(Double.class) == 0)
        {
            return new EffectiveValueImpl(CellType.STRING, "NaN");
        }

        double result = leftValue.extractValueWithExpectation(Double.class) / rightValue.extractValueWithExpectation(Double.class);

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
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

