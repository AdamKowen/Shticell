package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class SumExpression implements Expression {
    private final String rangeName;

    public SumExpression(String rangeName) {
        this.rangeName = rangeName;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        double sum = 0;
        try {
            List<Cell> cellsInRange = sheet.getCellsInRange(rangeName);
            for (Cell cell :cellsInRange ) {
                Expression expr= cell.getExpression();
                if(expr.eval(sheet).getCellType()== CellType.NUMERIC) {
                    sum += expr.eval(sheet).extractValueWithExpectation(Double.class);
                    System.out.println(expr.eval(sheet).extractValueWithExpectation(Double.class));
                }
            }
        }catch (Exception e){
            throw new RuntimeException("error"+e.getMessage());
        }


        return new EffectiveValueImpl(CellType.NUMERIC, sum);
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
