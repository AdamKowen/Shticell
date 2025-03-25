package permissions;

public class SheetPermission {

    private String username;
    private PermissionType type;   // type of permission
    private boolean approvedByOwner; // if the permission approved or not

    public SheetPermission(String username, PermissionType type) {
        this.username = username;
        this.type = type;
        this.approvedByOwner = false; // default if not approved
    }

    // Getters and setters
    public String getUsername() { return username; }

    public PermissionType getType() { return type; }

    public boolean isApprovedByOwner() { return approvedByOwner; }

    public void approve() {
        this.approvedByOwner = true;
    }

    public boolean isEditable() {
        return type == PermissionType.OWNER || type == PermissionType.WRITER;
    }

    public boolean isViewable() {
        return type == PermissionType.OWNER || type == PermissionType.READER || type == PermissionType.WRITER;
    }

    public void setType(PermissionType newType) {
        this.type = newType;
    }
}

