package sheet.cell.api;

public class CellStyle {
    private String backgroundColor; // יכול להיות null אם אין עיצוב
    private String textColor;       // יכול להיות null אם אין עיצוב
    private TextAlignment alignment; // CENTER, LEFT, RIGHT
    private boolean isWrapped;      // האם לבצע wrap

    // Constructor ריק
    public CellStyle() {
        setToDefault();         // ברירת מחדל ללא wrap
    }

    // פונקציה פנימית שמגדירה ערכי ברירת מחדל (בשימוש בקונסטרוקטור וב-resetToDefault)
    public void setToDefault() {
        this.backgroundColor = "#FFFFFF"; // לבן כרקע ברירת מחדל
        this.textColor = "#000000";       // שחור כצבע טקסט ברירת מחדל
        this.alignment = TextAlignment.LEFT; // יישור שמאלי כברירת מחדל
        this.isWrapped = false;           // ברירת מחדל ללא wrap
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
        return this.textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    // Getter and Setter עבור alignment
    public TextAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(TextAlignment alignment) {
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

