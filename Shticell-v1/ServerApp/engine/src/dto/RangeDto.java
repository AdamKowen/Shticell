package dto;

import sheet.range.api.Range;
import sheet.range.boundaries.Boundaries;

public class RangeDto {
    private BoundariesDto boundaries;
    private String name;

    public RangeDto(Boundaries boundaries, String name) {
        this.boundaries = new BoundariesDto(boundaries);
        this.name = name;
    }

    public RangeDto(Range og) {
        this.boundaries = new BoundariesDto(og.getBoundaries());
        this.name = og.getName();
    }


    public BoundariesDto getBoundaries() {
        return boundaries;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Range: "
                 + name  +
                " from " + boundaries.getFrom() +
                " to " + boundaries.getTo();
    }
}
