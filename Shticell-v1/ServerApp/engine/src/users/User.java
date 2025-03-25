package users;

import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private List<String> sheets; // lists of sheet names in users data
    private SheetEngine sheetEngine;

    public User(String username) {
        this.username = username;
        this.sheets = new ArrayList<>();
        this.sheetEngine=new SheetEngineImpl();
    }

    public String getUsername() {
        return username;
    }

    public List<String> getSheets() {
        return sheets;
    }

    // adding new sheet to users account
    public void addSheet(String sheetName) {
        if (!sheets.contains(sheetName)) {
            sheets.add(sheetName);
        }
    }

    // checking if user has access to a certain sheet
    public boolean hasAccessToSheet(String sheetName) {
        return sheets.contains(sheetName);
    }

    public SheetEngine getSheetEngine() {
        return sheetEngine;
    }

    public void setSheetEngine(SheetEngine sheetEngine) {
        this.sheetEngine = sheetEngine;
    }
}

