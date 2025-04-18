package sheet.cell.impl;

import expression.api.Expression;
import sheet.cell.api.CellType;
import sheet.cell.api.EffectiveValue;

import java.io.Serializable;

public class EffectiveValueImpl implements EffectiveValue, Serializable {

    private CellType cellType;
    private Object value;

    public EffectiveValueImpl(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
    }

    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public Object getValue() {
        return value;
    }


    @Override
    public <T> T extractValueWithExpectation(Class<T> type) {
        if (cellType.isAssignableFrom(type)) {
            return type.cast(value);
        }
        // error handling... exception ? return null ?
        return null;
    }
}