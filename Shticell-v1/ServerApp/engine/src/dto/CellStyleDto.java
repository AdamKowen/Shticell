package dto;

import sheet.cell.api.CellStyle;

public class CellStyleDto {
    private String backgroundColor;
    private String textColor;
    private String alignment; // aligmnemt as string to support transmition
    private boolean isWrapped;


    // full contractor
    public CellStyleDto(CellStyle cellStyle) {
        this.backgroundColor = cellStyle.getBackgroundColor();
        this.textColor = cellStyle.getTextColor();
        this.alignment = cellStyle.getAlignment().toString();
        this.isWrapped = cellStyle.isWrapped();
    }

    public CellStyleDto(String backgroundColor, String textColor, String alignment, boolean isWrapped) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.alignment = alignment;
        this.isWrapped = isWrapped;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public String getAlignment() {
        return alignment;
    }

    public boolean isWrapped() {
        return isWrapped;
    }

}