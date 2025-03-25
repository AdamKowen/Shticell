package expression.impl;

import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public class SubExpression implements Expression {

    private final Expression source;
    private final Expression startIndex;
    private final Expression endIndex;

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        this.source = source;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public EffectiveValue eval(SheetReadActions sheet) {
        EffectiveValue sourceEffectiveValue = source.eval(sheet);
        EffectiveValue startEffectiveValue = startIndex.eval(sheet);
        EffectiveValue finishEffectiveValue = endIndex.eval(sheet);

        // checks all type of data if it valid
        if (!sourceEffectiveValue.getCellType().equals(CellType.STRING) || !startEffectiveValue.getCellType().equals(CellType.NUMERIC) || !finishEffectiveValue.getCellType().equals(CellType.NUMERIC))
        {
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        Double startD = startEffectiveValue.extractValueWithExpectation(Double.class);
        Double endD = finishEffectiveValue.extractValueWithExpectation(Double.class);

        if (startD % 1 != 0 || endD % 1 != 0) {
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        String src = sourceEffectiveValue.extractValueWithExpectation(String.class);
        int start = startD.intValue();
        int end = endD.intValue();

        // check for "!UNDEFINED!" in the source

        if (src.equals("!UNDEFINED!") || src.equals("NaN")) {
            // undefined if one value at least is undefined
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }


        // index check
        if (start < 0 || end >= src.length() || start > end) {
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }

        String result  = src.substring(start, end + 1);
        return new EffectiveValueImpl(CellType.STRING, result);
    }

    private Boolean checkValidation(EffectiveValue sourceEffectiveValue, EffectiveValue startEffectiveValue, EffectiveValue finishEffectiveValue)
    {

        if(!source.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!sourceEffectiveValue.getCellType().equals(CellType.STRING))
            {
                return false;
            }
        }

        if(!startIndex.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!startEffectiveValue.getCellType().equals(CellType.NUMERIC))
            {
                return false;
            }
        }

        if(!endIndex.doesContainRef()) //doesn't contain REF and also NOT a string, func invalid
        {
            if (!finishEffectiveValue.getCellType().equals(CellType.NUMERIC))
            {
                return false;
            }
        }

        return true; //otherwise, the func is ok or undefined. but not invalid
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }

    @Override
    public Boolean doesContainRef(){
        return source.doesContainRef() || startIndex.doesContainRef() || endIndex.doesContainRef();
    }

    @Override
    public void collectDependencies(List<Coordinate> dependencies)
    {
        if (source instanceof RefExpression)
        {
            dependencies.add(((RefExpression) source).getRefValue());
        }
        else {
            source.collectDependencies(dependencies);
        }

        if (startIndex instanceof RefExpression)
        {
            dependencies.add(((RefExpression) startIndex).getRefValue());
        }
        else {
            startIndex.collectDependencies(dependencies);
        }


        if (endIndex instanceof RefExpression)
        {
            dependencies.add(((RefExpression) endIndex).getRefValue());
        }
        else {
            endIndex.collectDependencies(dependencies);
        }
    }
}

