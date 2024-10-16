package users;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private List<String> sheets; // רשימת שמות הגיליונות שהמשתמש העלה

    public User(String username) {
        this.username = username;
        this.sheets = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public List<String> getSheets() {
        return sheets;
    }

    // הוספת גיליון חדש למשתמש
    public void addSheet(String sheetName) {
        if (!sheets.contains(sheetName)) {
            sheets.add(sheetName);
        }
    }

    // בדיקה האם למשתמש יש הרשאה לגיליון מסוים
    public boolean hasAccessToSheet(String sheetName) {
        return sheets.contains(sheetName);
    }
}

