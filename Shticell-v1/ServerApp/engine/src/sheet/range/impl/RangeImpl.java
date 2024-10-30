package sheet.range.impl;

import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.coordinate.api.Coordinate;
import sheet.range.boundaries.Boundaries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RangeImpl implements sheet.range.api.Range, Serializable {
    private Boundaries boundaries;
    private String name;


   public RangeImpl(Boundaries boundaries, String name) {
       this.boundaries = boundaries;
       this.name = name;
   }
    @Override
    public Boundaries getBoundaries() {
        return boundaries;
    }
    @Override
    public String getName() {
        return name;
    }

    public void setBoundaries(Boundaries boundaries) {
        this.boundaries = boundaries;
    }

    public void setName(String name) {
        this.name = name;
    }

}

