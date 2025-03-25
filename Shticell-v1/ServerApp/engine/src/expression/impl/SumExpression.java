package expression.impl;


import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;
import sheet.range.api.Range;

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
            if(!sheet.getRanges().keySet().contains(rangeName)){
                throw new RuntimeException("Range name "+rangeName+" not found");
            }
            else if(!isRangeInBoundaries(sheet,rangeName)){
                throw new RuntimeException("Range "+rangeName+" is out of bounds");

            }
            else
                throw new RuntimeException("Error: "+e.getMessage());
        }


        return new EffectiveValueImpl(CellType.NUMERIC, sum);
    }

    private boolean isRangeInBoundaries(SheetReadActions sheet, String rangeName) {
        // gets range from sheet
        Range range = sheet.getRanges().get(rangeName);
        if (range == null) {
            // if doesnt exist return false
            return false;
        }

        // gets the num of rows and cols
        int maxRows = sheet.getNumOfRows();
        int maxColumns = sheet.getNumOfColumns();

        // gets the edge coordinates
        int[] topLeftCoords = convertToCoordinates(range.getBoundaries().getFrom());
        int[] bottomRightCoords = convertToCoordinates(range.getBoundaries().getTo());

        // checks the coordinates if in range
        return topLeftCoords[0] >= 0 && topLeftCoords[1] >= 0 &&
                bottomRightCoords[0] < maxRows && bottomRightCoords[1] < maxColumns;
    }

    // conversion from string to coordinate
    private int[] convertToCoordinates(String cell) {
        int row = -1;
        int column = -1;

        if (Character.isDigit(cell.charAt(0))) {
            row = Integer.parseInt(cell.substring(0, 1)) - 1;
            column = cell.charAt(1) - 'A';
        } else if (Character.isLetter(cell.charAt(0))) {
            column = cell.charAt(0) - 'A';
            row = Integer.parseInt(cell.substring(1)) - 1;
        }

        return new int[]{row, column};
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
