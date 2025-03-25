package sheet.cell.api;

import java.io.Serializable;

public class CellStyle implements Serializable {
    private String backgroundColor; // null = no color
    private String textColor;       // null = no color
    private TextAlignment alignment; // CENTER, LEFT, RIGHT
    private boolean isWrapped;      // wrap

    // empty Constructor
    public CellStyle() {
        setToDefault();
    }

    // Sets default values - used in constructor and in reset functions
    public void setToDefault() {
        this.backgroundColor = "#FFFFFF"; // white backgroung
        this.textColor = "#000000";       // black text
        this.alignment = TextAlignment.LEFT; // aligned left
        this.isWrapped = false;           // no wrap
    }

    //  backgroundColor
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    //  textColor
    public String getTextColor() {
        return this.textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    // returns alignment as string
    public String getAlignment() {
        return alignment != null ? alignment.name() : null;
    }

    // gets string sets as enum
    public void setAlignment(String alignment) {
        if (alignment != null) {
            try {
                this.alignment = TextAlignment.valueOf(alignment.toUpperCase()); // from string to enum
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid alignment value: " + alignment);
                this.alignment = TextAlignment.LEFT; // default left
            }
        } else {
            this.alignment = null; // if string is null
        }
    }

    //  isWrapped
    public boolean isWrapped() {
        return isWrapped;
    }

    public void setWrapped(boolean isWrapped) {
        this.isWrapped = isWrapped;
    }


}

