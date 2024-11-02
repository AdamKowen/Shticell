package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permissions.PermissionManager;
import permissions.PermissionRequest;
import permissions.RequestStatus;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet(name = "ApprovalRequestServlet", urlPatterns = {"/approval-request"})
public class ApprovalRequestServlet extends HttpServlet {

    private final UserManager userManager = ServletUtils.getUserManager(getServletContext());
    private final PermissionManager permissionManager = userManager.getPermissionManager();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // פרמטרים מהבקשה
        String targetUsername = request.getParameter("username");
        String sheetName = request.getParameter("sheetName");
        String status = request.getParameter("status");

        // בדיקה אם כל הפרמטרים קיימים
        if (targetUsername == null || sheetName == null || status == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: username, sheetName, or status");
            return;
        }

        try {
            // המרת הסטטוס ל-Enum
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());

            // מציאת הבקשה
            PermissionRequest permissionRequest = findRequest(targetUsername, sheetName);

            if (permissionRequest != null) {
                // עדכון סטטוס הבקשה
                permissionManager.updateRequestStatus(permissionRequest, requestStatus);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Request status updated.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Request not found for specified username and sheetName.");
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid request status: must be 'accepted' or 'rejected'");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error updating request: " + e.getMessage());
        }
    }

    // פונקציה למציאת הבקשה לפי משתמש וגיליון
    private PermissionRequest findRequest(String username, String sheetName) {
        return permissionManager.getPendingRequests().stream()
                .filter(req -> req.getRequesterUsername().equals(username) && req.getSheetName().equals(sheetName))
                .findFirst()
                .orElse(null);
    }
}
