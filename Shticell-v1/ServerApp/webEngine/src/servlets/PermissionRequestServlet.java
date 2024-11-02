package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permissions.PermissionManager;
import permissions.PermissionRequest;
import permissions.PermissionType;
import permissions.RequestStatus;
import users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "PermissionRequestServlet", urlPatterns = {"/permission-requests"})
public class PermissionRequestServlet extends HttpServlet {

    private UserManager userManager;
    private PermissionManager permissionManager;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        userManager = ServletUtils.getUserManager(getServletContext());
        permissionManager = userManager.getPermissionManager();
    }

    // שליחת בקשה חדשה
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        String sheetName = request.getParameter("sheetName");
        String requestedPermissionType = request.getParameter("type");

        if (sheetName == null || requestedPermissionType == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters");
            return;
        }

        try {
            PermissionType permissionType = PermissionType.valueOf(requestedPermissionType.toUpperCase());
            PermissionRequest permissionRequest = new PermissionRequest(username, sheetName, permissionType);
            permissionManager.addPermissionRequest(permissionRequest);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Permission request submitted successfully.");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid permission type");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error processing request: " + e.getMessage());
        }
    }

    // הצגת רשימת בקשות ממתינות לאישור
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            List<PermissionRequest> pendingRequests = permissionManager.getPendingRequests();
            out.write(gson.toJson(pendingRequests));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error retrieving requests: " + e.getMessage());
        }
    }

    // עדכון סטטוס בקשה (אישור או דחייה)
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String targetUsername = request.getParameter("username"); // המשתמש שעבורו נרצה לאשר את הבקשה
        String sheetName = request.getParameter("sheetName");
        String status = request.getParameter("status");

        // בדיקה שכל הפרמטרים הנדרשים קיימים
        if (targetUsername == null || sheetName == null || status == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: username, sheetName, or status");
            return;
        }

        try {
            // המרת סטטוס הבקשה לסוג ה-Enum
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());

            // מציאת הבקשה עבור המשתמש והגיליון הנכונים
            PermissionRequest permissionRequest = findRequest(targetUsername, sheetName);

            if (permissionRequest != null) {
                // עדכון הסטטוס של הבקשה
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

    // פונקציה למציאת בקשה על פי שם משתמש וגיליון
    private PermissionRequest findRequest(String username, String sheetName) {
        return permissionManager.getPendingRequests().stream()
                .filter(req -> req.getRequesterUsername().equals(username) && req.getSheetName().equals(sheetName))
                .findFirst()
                .orElse(null);
    }
}
