package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class RefExpression implements Expression {

    private final Coordinate coordinate;

    public RefExpression(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {

        int row  = coordinate.getRow();
        int column = coordinate.getColumn();
        Cell refCell = sheet.getCell(row, column);

        if(refCell == null) {
            if(row < 1 || row > sheet.getNumOfRows() || column < 1 || column > sheet.getNumOfColumns()){
                throw new IllegalArgumentException("Invalid argument types for REF function. Cell is out of range.");
            }
            else
            {

                return new EffectiveValueImpl(CellType.UNKNOWN, "!UNDEFINED!"); //empty cell
            }
        }

        if(refCell.getExpression() instanceof SumExpression){
            SumExpression resE=(SumExpression)refCell.getExpression();
            return resE.eval(sheet);
        }
        else if(refCell.getExpression() instanceof AverageExpression){
            AverageExpression avg =(AverageExpression)refCell.getExpression();
            return avg.eval(sheet);
        }

        return refCell.getEffectiveValue();

    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.UNKNOWN;
    }

    public Coordinate getRefValue() {
        return coordinate;
    }

    @Override
    public Boolean doesContainRef(){
        return true;
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        dependencies.add(this.getRefValue());
    }

}