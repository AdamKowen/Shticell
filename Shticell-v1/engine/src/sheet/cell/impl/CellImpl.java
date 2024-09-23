package sheet.cell.impl;

import expression.api.Expression;
import expression.parser.FunctionParser;
import sheet.api.SheetReadActions;
import sheet.cell.api.Cell;

import java.util.ArrayList;
import java.util.List;

import sheet.cell.api.CellStyle;
import sheet.cell.api.EffectiveValue;
import sheet.coordinate.api.*;
import sheet.coordinate.impl.CoordinateImpl;


public class CellImpl implements Cell {

    private final Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    private List<Coordinate> dependsOn;
    private List<Coordinate> influencingOn;
    private CellStyle style;

    public CellImpl(int row, int column, String originalValue, int version)  {
        this.coordinate = new CoordinateImpl(row, column);
        this.originalValue = originalValue;
        this.version = version;
        this.dependsOn = new ArrayList<>();
        this.influencingOn = new ArrayList<>();

        this.style = new CellStyle();

    }
    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public void setCellOriginalValue(String value, int currVersion) {
        if (!this.originalValue.equals(value)) //if value is different update
        {
            this.version = currVersion;
        }

        this.originalValue = value;

    }

    @Override
    public EffectiveValue getEffectiveValue() {
        return effectiveValue;
    }


    @Override
    public Expression getExpression() {
        return FunctionParser.parseExpression(originalValue);
    }


    @Override
    public void calculateEffectiveValue(SheetReadActions sheet) {
        // build the expression object out of the original value...
        // it can be {PLUS, 4, 5} OR {CONCAT, hello, world}
        Expression expression = FunctionParser.parseExpression(originalValue);

        // second question: what is the return type of eval() ?
        EffectiveValue newCreatedValue = expression.eval(sheet);

        if (effectiveValue != null){
            if (newCreatedValue.getValue() != effectiveValue.getValue())
            {
                this.version = sheet.getVersion() + 1; // add one for new version
            }
        }
        else{
            if (sheet.getVersion()==0)
            {
                this.version = 1;
            }
        }

        effectiveValue = newCreatedValue;
    }


    @Override
    public boolean doesContainRef() {
        // build the expression object out of the original value...
        // it can be {PLUS, 4, 5} OR {CONCAT, hello, world}
        Expression expression = FunctionParser.parseExpression(originalValue);


        return expression.doesContainRef();
    }


    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public List<Coordinate> getDependentCells() {
        return dependsOn;
    }

    @Override
    public List<Coordinate> getInfluencedCells() {
        return influencingOn;
    }


    @Override
    public void addDependentCell(Coordinate cell) {
        dependsOn.add(cell);
    }

    @Override
    public void addInfluencedCell(Coordinate cell) {
        influencingOn.add(cell);
    }

    @Override
    public void setDependentCells(List<Coordinate> updatedList) {
        this.dependsOn = updatedList;
    }

    @Override
    public void setInfluencedCells(List<Coordinate> updatedList) {
        this.influencingOn = updatedList;
    }
}