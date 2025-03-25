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

        try {
            List<Cell> cellsInRange = sheet.getCellsInRange(rangeName);
            for (Cell cell : cellsInRange) {
                Expression expr = cell.getExpression();
                if (expr.getFunctionResultType() == CellType.NUMERIC) {
                    sum += expr.eval(sheet).extractValueWithExpectation(Double.class);
                    count++;
                }
            }
            if (count == 0) {
                //throw new IllegalArgumentException("No numeric cells found in the range.");
                return new EffectiveValueImpl(CellType.NUMERIC, 0);
            }

        }
        catch (Exception e) {
            if(!sheet.getRanges().keySet().contains(rangeName)){
                throw new RuntimeException("Range name "+rangeName+" not found");
            }
            else if(!isRangeInBoundaries(sheet,rangeName)){
                throw new RuntimeException("Range "+rangeName+" is out of bounds");

            }
            else
                throw new RuntimeException("Error: "+e.getMessage());
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

    private boolean isRangeInBoundaries(SheetReadActions sheet, String rangeName) {
        // getting ranges from the sheet
        Range range = sheet.getRanges().get(rangeName);
        if (range == null) {
            //  if range doesnt exist return false
            return false;
        }

        // gets num of rows and cols
        int maxRows = sheet.getNumOfRows();
        int maxColumns = sheet.getNumOfColumns();

        // gets the edge coordinates turns them into coordinates
        int[] topLeftCoords = convertToCoordinates(range.getBoundaries().getFrom());
        int[] bottomRightCoords = convertToCoordinates(range.getBoundaries().getTo());

        // checks if ranges ok according to sheet
        return topLeftCoords[0] >= 0 && topLeftCoords[1] >= 0 &&
                bottomRightCoords[0] < maxRows && bottomRightCoords[1] < maxColumns;
    }

    // converts string to coordinates
    private int[] convertToCoordinates(String cell) {
        int row = -1;
        int column = -1;

        // checks if starts with letter or number
        if (Character.isDigit(cell.charAt(0))) {
            row = Integer.parseInt(cell.substring(0, 1)) - 1;  // roe (starts from 0)
            column = cell.charAt(1) - 'A';                     // col
        } else if (Character.isLetter(cell.charAt(0))) {
            column = cell.charAt(0) - 'A';                     // col
            row = Integer.parseInt(cell.substring(1)) - 1;     // row (starts from 0)
        }


        return new int[]{row, column};
    }

}

