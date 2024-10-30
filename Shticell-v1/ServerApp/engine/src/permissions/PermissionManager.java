package permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {
    private Map<String, List<SheetPermission>> permissions; // מיפוי שמות גיליונות לרשימות של הרשאות

    public PermissionManager() {
        permissions = new HashMap<>();
    }

    // הוספת הרשאה חדשה למשתמש על גיליון
    public void addPermission(String sheetName, SheetPermission permission) {
        permissions.computeIfAbsent(sheetName, k -> new ArrayList<>()).add(permission);
    }

    // עדכון הרשאה קיימת
    public void updatePermission(String sheetName, String username, PermissionType newType) {
        List<SheetPermission> sheetPermissions = permissions.get(sheetName);
        if (sheetPermissions != null) {
            for (SheetPermission perm : sheetPermissions) {
                if (perm.getUsername().equals(username)) {
                    perm.setType(newType);
                    break;
                }
            }
        }
    }

    // בדיקת הרשאה
    public PermissionType getPermissionType(String sheetName, String username) {
        List<SheetPermission> sheetPermissions = permissions.get(sheetName);
        if (sheetPermissions != null) {
            for (SheetPermission perm : sheetPermissions) {
                if (perm.getUsername().equals(username)) {
                    return perm.getType();
                }
            }
        }
        return PermissionType.NONE;
    }


    public boolean hasEditPermission(String sheetName, String username) {
        PermissionType type = getPermissionType(sheetName, username);
        return type == PermissionType.OWNER || type == PermissionType.WRITER;
    }

    public boolean hasViewPermission(String sheetName, String username) {
        PermissionType type = getPermissionType(sheetName, username);
        return type != PermissionType.NONE;
    }

    public List<SheetPermission> getPermissionsForSheet(String sheetName) {
        return this.permissions.get(sheetName);
    }
}

