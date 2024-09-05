package expression.impl;
import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;
import java.util.Set;


public class AbsExpression implements Expression {

    private Expression arg;

    public AbsExpression(Expression arg) {
        this.arg = arg;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue argValue = arg.eval(sheet);

        if(!argValue.getCellType().equals(CellType.NUMERIC)){
            if (arg.doesContainRef())
            {
                return new EffectiveValueImpl(CellType.STRING, "NaN"); //in the future a user can fix the problem
            }
            else{
                throw new IllegalArgumentException("Invalid argument types for ABS function. Expected NUMERIC, but got " + argValue.getCellType());
            }
        }

        double result = argValue.extractValueWithExpectation(Double.class);
        if (result<0)
            result = ((-1) * result);
        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }


    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }

    @Override
    public Boolean doesContainRef(){
        return arg.doesContainRef();
    }


    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (arg instanceof RefExpression)
        {
            dependencies.add(((RefExpression) arg).getRefValue());
        }
        else{
            arg.collectDependencies(dependencies);
        }
    }

}





