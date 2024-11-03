package permissions;

import dto.PermissionDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionManager {
    private Map<String, List<SheetPermission>> permissions; // מיפוי שמות גיליונות לרשימות של הרשאות
    private List<PermissionRequest> requests = new ArrayList<>();

    public PermissionManager() {
        permissions = new HashMap<>();
        requests=new ArrayList<>();
    }

    // הוספת בקשת הרשאה חדשה
    public void addPermissionRequest(PermissionRequest request) {
        requests.add(request);
    }

    // קבלת רשימת בקשות
    public List<PermissionRequest> getPendingRequests() {
        return requests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).collect(Collectors.toList());
    }

    // אישור או דחיית בקשה
    public void updateRequestStatus(PermissionRequest request, RequestStatus newStatus) {
        request.setStatus(newStatus);
        if (newStatus == RequestStatus.APPROVED) {
            SheetPermission newPermission=new SheetPermission(request.getRequesterUsername(),request.getRequestedPermission());
            addPermission(request.getSheetName(), newPermission);
        }
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

    public List<PermissionDTO> getPermissionsForSheetDTO(String sheetName) {
        List<PermissionDTO> dtoList = new ArrayList<>();

        // הוספת הרשאות
        List<SheetPermission> sheetPermissions = permissions.getOrDefault(sheetName, new ArrayList<>());
        for (SheetPermission perm : sheetPermissions) {
            dtoList.add(new PermissionDTO(perm.getUsername(), perm.getType().toString(), "Granted"));
        }

        // הוספת בקשות ממתינות
        for (PermissionRequest req : requests) {
            if (req.getSheetName().equals(sheetName) && req.getStatus() == RequestStatus.PENDING) {
                dtoList.add(new PermissionDTO(req.getRequesterUsername(), req.getRequestedPermission().toString(), "Pending"));
            }
        }

        return dtoList;
    }
}

