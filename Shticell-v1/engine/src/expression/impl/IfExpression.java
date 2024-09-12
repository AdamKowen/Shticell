package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class IfExpression implements Expression {
    private final Expression condition;
    private final Expression thenExpr;
    private final Expression elseExpr;

    public IfExpression(Expression condition, Expression thenExpr, Expression elseExpr) {
        this.condition = condition;
        this.thenExpr = thenExpr;
        this.elseExpr = elseExpr;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue conditionValue = condition.eval(sheet);

        // Validate condition type
        if (!conditionValue.getCellType().equals(CellType.BOOLEAN)) {
           // throw new IllegalArgumentException("Invalid argument type for IF function. Expected BOOLEAN for condition, but got " + condition.getFunctionResultType());
            return new EffectiveValueImpl(CellType.STRING, "UNKNOWN");
        }

        // Validate return types
        CellType thenType = thenExpr.eval(sheet).getCellType();
        CellType elseType = elseExpr.eval(sheet).getCellType();
        if (thenType != elseType && !thenType.equals(CellType.UNKNOWN) && !elseType.equals(CellType.UNKNOWN)) {
            //throw new IllegalArgumentException("Invalid argument types for IF function. THEN and ELSE must return the same type.");
            return new EffectiveValueImpl(CellType.STRING, "UNKNOWN");
        }

        if (conditionValue.extractValueWithExpectation(Boolean.class)) {
            return thenExpr.eval(sheet);
        } else {
            return elseExpr.eval(sheet);
        }
    }

    @Override
    public CellType getFunctionResultType() {
        CellType thenType = thenExpr.getFunctionResultType();
        CellType elseType = elseExpr.getFunctionResultType();
        if (thenType != elseType) {
            throw new IllegalArgumentException("IF expression's THEN and ELSE expressions must return the same type.");
        }
        return thenType;
    }
    @Override
    public Boolean doesContainRef(){
        return thenExpr.doesContainRef() || elseExpr.doesContainRef();
    }



    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (elseExpr instanceof RefExpression)
        {
            dependencies.add(((RefExpression) elseExpr).getRefValue());
        }
        else {
            elseExpr.collectDependencies(dependencies);
        }

        if (thenExpr instanceof RefExpression)
        {
            dependencies.add(((RefExpression) thenExpr).getRefValue());
        }
        else {
            thenExpr.collectDependencies(dependencies);
        }
    }
}
