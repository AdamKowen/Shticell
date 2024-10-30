package permissions;

public class SheetPermission {
    private String username;       // שם המשתמש
    private PermissionType type;   // סוג ההרשאה
    private boolean approvedByOwner; // האם ההרשאה אושרה ע"י הבעלים

    public SheetPermission(String username, PermissionType type) {
        this.username = username;
        this.type = type;
        this.approvedByOwner = false; // ברירת המחדל, לא מאושר
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

