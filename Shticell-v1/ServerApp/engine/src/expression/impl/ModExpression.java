
package expression.impl;
import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;


public class ModExpression implements Expression {

    private Expression left;
    private Expression right;

    public ModExpression(Expression left, Expression right) {
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

        double result = leftValue.extractValueWithExpectation(Double.class) % rightValue.extractValueWithExpectation(Double.class);

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    private Boolean checkValidation(EffectiveValue evalLeft, EffectiveValue evalRight)
    {

        if(!left.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!evalLeft.getCellType().equals(CellType.NUMERIC))
            {
                return false;
            }
        }

        if(!right.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!evalRight.getCellType().equals(CellType.NUMERIC))
            {
                return false;
            }
        }

        return true; //otherwise, the func is ok or undefined. but not invalid
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

