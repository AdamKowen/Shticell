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

    // Getter and Setter עבור backgroundColor
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    // Getter and Setter עבור textColor
    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    // Getter and Setter עבור alignment
    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    // Getter and Setter עבור isWrapped
    public boolean isWrapped() {
        return isWrapped;
    }

    public void setWrapped(boolean isWrapped) {
        this.isWrapped = isWrapped;
    }
}