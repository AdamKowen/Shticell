package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class ConcatExpression implements Expression {

    private final Expression left;
    private final Expression right;

    public ConcatExpression(Expression valueLeft, Expression valueRight) {
        this.left = valueLeft;
        this.right = valueRight;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue evalRight = right.eval(sheet);
        EffectiveValue evalLeft = left.eval(sheet);

        if (!evalLeft.getCellType().equals(CellType.STRING) || !evalLeft.getCellType().equals(CellType.STRING)) { //there is a problem with at least one arg

            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        String strLeft = evalLeft.extractValueWithExpectation(String.class);
        String strRight = evalRight.extractValueWithExpectation(String.class);

        // checks of string has "!UNDEFINED!"
        if (strLeft.equals("!UNDEFINED!") || strRight.equals("!UNDEFINED!") ||
        strLeft.equals("NaN") || strRight.equals("Nan")){
            // if has "!UNDEFINED!" so the whole value is undefined
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        String stitchedResult = strLeft + strRight;
        return new EffectiveValueImpl(CellType.STRING, stitchedResult);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
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

