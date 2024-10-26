package dto;

import sheet.cell.api.Cell;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.api.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class CellDtoImpl implements CellDto {
    private Coordinate coordinate;
    private String originalValue;
    private EffectiveValue effectiveValue;
    private int version;
    private List<Coordinate> dependsOn;
    private List<Coordinate> influencingOn;
    private CellStyleDto style;
    private String lastUserUpdated;

    // Constructor
    public CellDtoImpl(Cell cell) {
        this.coordinate = cell.getCoordinate();
        this.originalValue = cell.getOriginalValue();
        this.effectiveValue = cell.getEffectiveValue();
        this.version = cell.getVersion();
        this.dependsOn = new ArrayList<>(cell.getDependentCells());
        this.influencingOn = new ArrayList<>(cell.getInfluencedCells());
        this.style = new CellStyleDto(cell.getStyle());
        this.lastUserUpdated = cell.getLastUserUpdated();
    }

    // Getters and Setters
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public String getValue() {
        Object curr = effectiveValue.getValue();
        if (curr != null)
        {
            return curr.toString();
        }
        else
        {
            return null;
        }
    }

    public int getVersion() {
        return version;
    }

    public List<Coordinate> getDependsOn() {
        return dependsOn;
    }

    public List<Coordinate> getInfluencingOn() {
        return influencingOn;
    }

    public CellStyleDto getStyle() {
        return style;
    }

    public String getLastUserUpdated() {
        return lastUserUpdated;
    }


    public CellDtoImpl(Coordinate coordinate, String originalValue, EffectiveValue effectiveValue, int version, List<Coordinate> dependsOn, List<Coordinate> influencingOn, CellStyleDto style, String lastUserUpdated) {
        this.coordinate = coordinate;
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue; // שמירת ה-EffectiveValue המקורי
        this.version = version;
        this.dependsOn = new ArrayList<>(dependsOn);
        this.influencingOn = new ArrayList<>(influencingOn);
        this.style = style;
        this.lastUserUpdated = lastUserUpdated;
    }



}
