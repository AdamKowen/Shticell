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

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/permission-requests/*")
public class PermissionRequestServlet extends HttpServlet {


    UserManager userManager = ServletUtils.getUserManager(getServletContext());
    private final PermissionManager permissionManager = userManager.getPermissionManager();

    // שליחת בקשה חדשה
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requesterUsername = request.getParameter("username");
        String sheetName = request.getParameter("sheetName");
        String requestedPermissionType = request.getParameter("type");

        if (requesterUsername == null || sheetName == null || requestedPermissionType == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing parameters.");
            return;
        }

        PermissionType permissionType = PermissionType.valueOf(requestedPermissionType.toUpperCase());
        PermissionRequest permissionRequest = new PermissionRequest(requesterUsername, sheetName, permissionType);
        permissionManager.addPermissionRequest(permissionRequest);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Permission request submitted successfully.");
    }

    // הצגת רשימת בקשות ממתינות לאישור
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        var pendingRequests = permissionManager.getPendingRequests();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(new Gson().toJson(pendingRequests));
    }

    // עדכון סטטוס בקשה (אישור או דחייה)
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requesterUsername = request.getParameter("username");
        String sheetName = request.getParameter("sheetName");
        String status = request.getParameter("status");

        if (requesterUsername == null || sheetName == null || status == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing parameters.");
            return;
        }

        RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());
        PermissionRequest permissionRequest = findRequest(requesterUsername, sheetName);
        if (permissionRequest != null) {
            permissionManager.updateRequestStatus(permissionRequest, requestStatus);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Request status updated.");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("Request not found.");
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

