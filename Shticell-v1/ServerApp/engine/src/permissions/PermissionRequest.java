package permissions;

public class PermissionRequest {
    private String requesterUsername;
    private String sheetName;
    private PermissionType requestedPermission;
    private RequestStatus status;

    public PermissionRequest(String requesterUsername, String sheetName, PermissionType requestedPermission) {
        this.requesterUsername = requesterUsername;
        this.sheetName = sheetName;
        this.requestedPermission = requestedPermission;
        this.status = RequestStatus.PENDING;  // מצב התחלתי
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus newStatus) {
        status = newStatus;
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public PermissionType getRequestedPermission() {
        return requestedPermission;
    }

    // Getters and Setters
}

