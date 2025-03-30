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
        // user manager
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // getting user name from session
        String username = (String) request.getSession().getAttribute("username");
        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        // getting name from session
        User user = userManager.getUser(username);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not found.");
            return;
        }

        // getting all req
        PermissionManager permissionManager = userManager.getPermissionManager();

        // collecting all pending req
        List<PermissionRequest> userRequests = permissionManager.getPendingRequests().stream()
                .filter(req -> req.getRequesterUsername().equals(username))
                .collect(Collectors.toList());

        // if there is none return empty
        if (userRequests.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write(""); // מחרוזת ריקה
            return;
        }

        // bulding the json response
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

        // returning response to client
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
