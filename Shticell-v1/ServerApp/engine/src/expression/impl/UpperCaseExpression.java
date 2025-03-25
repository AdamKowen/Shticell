package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class UpperCaseExpression implements Expression {

    private final Expression e;

    public UpperCaseExpression(Expression value) {
        this.e = value;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue eval = e.eval(sheet);

        // more validations on the expected argument types
        if (!eval.getCellType().equals(CellType.STRING)) {
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        String upperCaseResult = eval.extractValueWithExpectation(String.class).toUpperCase();
        return new EffectiveValueImpl(CellType.STRING, upperCaseResult);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }

    @Override
    public Boolean doesContainRef(){
        return e.doesContainRef();
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (e instanceof RefExpression)
        {
            dependencies.add(((RefExpression) e).getRefValue());
        }
        else {
            e.collectDependencies(dependencies);
        }


    }
}
