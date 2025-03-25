package users;

import dto.SheetInfoDto;
import permissions.PermissionManager;
import permissions.PermissionType;
import permissions.SheetPermission;
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
    private PermissionManager permissionManager;
    private int sheetListVersion = 1;

    public UserManager() {
        usersSet = new HashMap<>();
        permissionManager = new PermissionManager();
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

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /// sheet dto info of all the users/
    public List<SheetInfoDto> getAllSheetsInfo() {
        List<SheetInfoDto> sheetList = new ArrayList<>();

        // run on all users in the system
        for (User user : usersSet.values()) {
            SheetEngine sheetEngine = user.getSheetEngine();

            // run on al sheet of curr user
            for (Sheet sheet : sheetEngine.getMyFiles().values()) {
                // creating sheet dto info of users sheet
                SheetInfoDto sheetInfo = new SheetInfoDto(
                        sheet.getName(),
                        sheet.getNumOfRows(),
                        sheet.getNumOfColumns(),
                        user.getUsername()  // name of owner user
                );
                sheetList.add(sheetInfo);
            }
        }

        return sheetList;
    }

    // adding permission
    public void addPermission(String sheetName, String username, PermissionType type) {
        SheetPermission permission = new SheetPermission(username, type);
        permissionManager.addPermission(sheetName, permission);
    }

    // updating permission
    public void updatePermission(String sheetName, String username, PermissionType newType) {
        permissionManager.updatePermission(sheetName, username, newType);
        updateSheetListVersion();
    }

    // check if user has edit access
    public boolean hasEditPermission(String sheetName, String username) {
        return permissionManager.hasEditPermission(sheetName, username);
    }

    // check if user has view access
    public boolean hasViewPermission(String sheetName, String username) {
        return permissionManager.hasViewPermission(sheetName, username);
    }

    // updating the version if there is a new one
    public void updateSheetListVersion() {
        sheetListVersion++;
    }

    public int getUpdateSheetListVersion() {
        return sheetListVersion;
    }

}
