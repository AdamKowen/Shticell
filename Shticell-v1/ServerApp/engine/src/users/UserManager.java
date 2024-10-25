package users;

import dto.SheetInfoDto;
import sheet.api.Sheet;
import sheet.impl.SheetImpl;
import sheetEngine.SheetEngine;

import java.util.*;

/*
Adding and retrieving users is synchronized and in that manner - these actions are thread safe
Note that asking if a user exists (isUserExists) does not participate in the synchronization and it is the responsibility
of the user of this class to handle the synchronization of isUserExists with other methods here on it's own
 */
public class UserManager {

    private final HashMap<String, User> usersSet;

    public UserManager() {
        usersSet = new HashMap<>();
    }

    public synchronized void addUser(String username) {
        usersSet.put(username,new User(username));
    }

    public synchronized void removeUser(String username) {
        usersSet.remove(username);
    }

    public synchronized Set<String> getUsers() {
        return Collections.unmodifiableSet(usersSet.keySet());
    }

    public synchronized User getUser(String username) {
        return usersSet.get(username);
    }

    public boolean isUserExists(String username) {
        return usersSet.containsKey(username);
    }


    // המתודה מחזירה רשימה של SheetInfoDto לכל הגיליונות של המשתמשים
    public List<SheetInfoDto> getAllSheetsInfo() {
        List<SheetInfoDto> sheetList = new ArrayList<>();

        // מעבר על כל המשתמשים במערכת (ערכים במפת HashMap)
        for (User user : usersSet.values()) {
            SheetEngine sheetEngine = user.getSheetEngine();

            // מעבר על כל הגיליונות במפת HashMap של המשתמש
            for (Sheet sheet : sheetEngine.getMyFiles().values()) {
                // יצירת SheetInfoDto עבור כל גיליון
                SheetInfoDto sheetInfo = new SheetInfoDto(
                        sheet.getName(),
                        sheet.getNumOfRows(),
                        sheet.getNumOfColumns(),
                        user.getUsername()  // הבעלים של הגיליון
                );
                sheetList.add(sheetInfo);
            }
        }

        return sheetList;
    }

}
