package sheet.cell.api;
import expression.api.Expression;
import sheet.api.SheetReadActions;
import sheet.coordinate.api.Coordinate;
import java.util.List;

public interface Cell {
    Coordinate getCoordinate();
    String getOriginalValue();
    List<Coordinate> getDependentCells();
    List<Coordinate> getInfluencedCells();

    void setCellOriginalValue(String value, int currVersion);
    EffectiveValue getEffectiveValue();
    Expression getExpression();

    boolean doesContainRef();
    void calculateEffectiveValue(SheetReadActions sheet);
    int getVersion();
    void addDependentCell(Coordinate cell);
    void addInfluencedCell(Coordinate cell);


    void setDependentCells(List<Coordinate> updatedList);
    void setInfluencedCells(List<Coordinate> updatedList);

    CellStyle getStyle();

    String getLastUserUpdated();
}
