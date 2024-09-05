package dto;
import sheet.cell.api.EffectiveValue;
import sheet.coordinate.api.Coordinate;

import java.util.List;

public interface CellDto {
    // Getters and Setters
    Coordinate getCoordinate();


    String getOriginalValue();


    String getValue();


    int getVersion();

    List<Coordinate> getDependsOn();

    List<Coordinate> getInfluencingOn();
}
