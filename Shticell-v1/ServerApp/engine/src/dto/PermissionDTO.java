package dto;

public class PermissionDTO {
    private String username;
    private String permission;
    private String status;

    public PermissionDTO(String username, String permission, String status) {
        this.username = username;
        this.permission = permission;
        this.status = status;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

