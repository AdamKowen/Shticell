package dto;

import sheet.range.boundaries.Boundaries;

public class BoundariesDto {
    private String from;
    private String to;

    public BoundariesDto(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public BoundariesDto(Boundaries og) {
        this.from = og.getFrom();
        this.to = og.getTo();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

}
