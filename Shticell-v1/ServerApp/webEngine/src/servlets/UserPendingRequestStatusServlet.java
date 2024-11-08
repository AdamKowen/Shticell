package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.User;
import users.UserManager;
import permissions.PermissionManager;
import permissions.PermissionRequest;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "UserPendingRequestStatusServlet", urlPatterns = {"/userPendingRequestStatus"})
public class UserPendingRequestStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // קבלת ה-UserManager מה-context
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // קבלת שם המשתמש מתוך הסשן
        String username = (String) request.getSession().getAttribute("username");
        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        // קבלת המשתמש מתוך UserManager
        User user = userManager.getUser(username);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not found.");
            return;
        }

        // קבלת PermissionManager לקבלת הבקשות של המשתמש
        PermissionManager permissionManager = userManager.getPermissionManager();

        // בדיקת בקשות הממתינות לאישור (או נדחו) עבור המשתמש
        List<PermissionRequest> userRequests = permissionManager.getPendingRequests().stream()
                .filter(req -> req.getRequesterUsername().equals(username))
                .collect(Collectors.toList());

        // אם אין בקשות למשתמש, מחזירים תגובה ריקה
        if (userRequests.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write(""); // מחרוזת ריקה
            return;
        }

        // יצירת תגובת JSON עם המידע על כל הבקשות והסטטוסים שלהן
        StringBuilder jsonResponse = new StringBuilder("[");
        for (int i = 0; i < userRequests.size(); i++) {
            PermissionRequest req = userRequests.get(i);
            jsonResponse.append("{")
                    .append("\"requestedPermission\":\"").append(req.getRequestedPermission()).append("\",")
                    .append("\"status\":\"").append(req.getStatus()).append("\"")
                    .append("}");
            if (i < userRequests.size() - 1) {
                jsonResponse.append(",");
            }
        }
        jsonResponse.append("]");

        // הגדרת סוג התוכן והחזרת התשובה ללקוח
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
