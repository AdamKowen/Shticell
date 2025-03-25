package dto;

public class SheetInfoDto {
    private String sheetName;
    private int numberOfRows;
    private int numberOfColumns;
    private String ownerName;
    private String access;

    // Constructor
    public SheetInfoDto(String sheetName, int numberOfRows, int numberOfColumns, String ownerName) {
        this.sheetName = sheetName;
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        this.ownerName = ownerName;
        this.access = "no access";  // default value
    }

    // Getters and Setters
    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public void setNumberOfRows(int numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getAccess() {
        return this.access;
    }

    public void setAccess(String UsersAccess) {
        this.access = UsersAccess;
    }


    // toString method for debugging
    @Override
    public String toString() {
        return "SheetInfoDto{" +
                "sheetName='" + sheetName + '\'' +
                ", numberOfRows=" + numberOfRows +
                ", numberOfColumns=" + numberOfColumns +
                ", ownerName='" + ownerName + '\'' +
                '}';
    }
}
