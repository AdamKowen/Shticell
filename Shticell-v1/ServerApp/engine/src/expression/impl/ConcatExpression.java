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

        // בדיקה אם src מכילה את המחרוזת "!UNDEFINED!"
        if (strLeft.equals("!UNDEFINED!") || strRight.equals("!UNDEFINED!") ||
        strLeft.equals("NaN") || strRight.equals("Nan")){
            // טיפול במקרה שבו המחרוזת היא "!UNDEFINED!"
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        String stitchedResult = strLeft + strRight;
        return new EffectiveValueImpl(CellType.STRING, stitchedResult);
    }

    private Boolean checkValidation(EffectiveValue evalLeft, EffectiveValue evalRight)
    {

        if(!left.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!evalLeft.getCellType().equals(CellType.STRING))
            {
                return false;
            }
        }

        if(!right.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!evalRight.getCellType().equals(CellType.STRING))
            {
                return false;
            }
        }

        return true; //otherwise, the func is ok or undefined value. but not invalid
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

