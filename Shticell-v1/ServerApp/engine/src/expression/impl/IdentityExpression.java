package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class IdentityExpression implements Expression {

    private final Object value;
    private final CellType type;

    public IdentityExpression(Object value, CellType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        return new EffectiveValueImpl(type, value);
    }

    @Override
    public CellType getFunctionResultType() {
        return type;
    }

    @Override
    public Boolean doesContainRef(){
        return false;
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        return; //doesn't have dependency
    }
}