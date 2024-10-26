package dto;

import sheet.cell.api.CellStyle;

public class CellStyleDto {
    private String backgroundColor;
    private String textColor;
    private String alignment; // נשתמש במחרוזת ליישור במקום ב-Enum כדי לשמור על עצמאות הממשק
    private boolean isWrapped;


    // קונסטרוקטור מלא
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


    // Getter and Setter עבור backgroundColor
    public String getBackgroundColor() {
        return backgroundColor;
    }

    // Getter and Setter עבור textColor
    public String getTextColor() {
        return textColor;
    }


    // Getter and Setter עבור alignment
    public String getAlignment() {
        return alignment;
    }


    // Getter and Setter עבור isWrapped
    public boolean isWrapped() {
        return isWrapped;
    }

}