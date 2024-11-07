package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permissions.PermissionManager;
import permissions.PermissionRequest;
import permissions.RequestStatus;
import sheetEngine.SheetEngine;
import users.User;
import users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "ApprovalRequestServlet", urlPatterns = {"/approval-request"})
public class ApprovalRequestServlet extends HttpServlet {

    private UserManager userManager;
    private PermissionManager permissionManager;

    @Override
    public void init() throws ServletException {
        super.init();
        userManager = ServletUtils.getUserManager(getServletContext());
        permissionManager = userManager.getPermissionManager();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String targetUsername = request.getParameter("username");
        String sheetName = request.getParameter("sheetName");
        String status = request.getParameter("status");

        // בדיקה שכל הפרמטרים הנדרשים קיימים
        if (targetUsername == null || sheetName == null || status == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: username, sheetName, or status");
            return;
        }

        try {
            // המרת הסטטוס ל-Enum
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());

            // מציאת הבקשה לפי המשתמש והגיליון
            PermissionRequest permissionRequest = findRequest(targetUsername, sheetName);

            if (permissionRequest != null) {
                // עדכון סטטוס הבקשה
                permissionManager.updateRequestStatus(permissionRequest, requestStatus);

                //user in session - approving request
                String username = SessionUtils.getUsername(request);
                User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
                SheetEngine sheetEngine = user.getSheetEngine();


                //user to pass the sheet to
                User userToPass = ServletUtils.getUserManager(getServletContext()).getUser(targetUsername);
                SheetEngine sheetEngineToPass = userToPass.getSheetEngine();


                sheetEngine.passSheetPermission(sheetName, sheetEngineToPass, permissionRequest.getRequestedPermission().toString());


                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Request status updated to: " + status);
                userManager.updateSheetListVersion();
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

    // פונקציה למציאת הבקשה לפי המשתמש והגיליון
    private PermissionRequest findRequest(String username, String sheetName) {
        return permissionManager.getPendingRequests().stream()
                .filter(req -> req.getRequesterUsername().equals(username) && req.getSheetName().equals(sheetName))
                .findFirst()
                .orElse(null);
    }
}
