package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;
import sheet.range.api.Range;
import sheet.range.impl.RangeImpl;

import java.util.List;

public class AverageExpression implements Expression {
    private final String rangeName;


    public AverageExpression(String rangeName) {
        this.rangeName=rangeName;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        double sum = 0;
        int count = 0;
        List<Cell> cellsInRange = sheet.getCellsInRange(rangeName);


        for (Cell cell : cellsInRange) {
            Expression expr= cell.getExpression();
            if (expr.getFunctionResultType()== CellType.NUMERIC) {
                sum += expr.eval(sheet).extractValueWithExpectation(Double.class);
                count++;
            }
        }
        if (count == 0) {
            //throw new IllegalArgumentException("No numeric cells found in the range.");
            return new EffectiveValueImpl(CellType.NUMERIC, 0);
        }

        return new EffectiveValueImpl(CellType.NUMERIC, sum / count);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }

    @Override
    public Boolean doesContainRef() {
        return false;
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies) {

    }
}

