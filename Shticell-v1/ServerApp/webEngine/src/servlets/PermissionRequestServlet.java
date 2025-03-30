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


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = SessionUtils.getUsername(request); // gets user in session
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        //gets the sheet name from req
        String sheetName = request.getParameter("sheetName");
        String requestedPermissionType = request.getParameter("type");

        // check for valid req
        if (sheetName == null || requestedPermissionType == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters");
            return;
        }

        try {
            //getting the permission type in enum from manager
            PermissionType permissionType = PermissionType.valueOf(requestedPermissionType.toUpperCase());
            PermissionRequest permissionRequest = new PermissionRequest(username, sheetName, permissionType);
            permissionManager.addPermissionRequest(permissionRequest); //adding permission


            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Permission request submitted successfully."); //sending validation to user
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid permission type");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error processing request: " + e.getMessage());
        }
    }

    // list of request waiting for approval of sheet from req
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

    // updatinf request (approve of reject)
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String targetUsername = request.getParameter("username"); // user name of the user we would like to approve his request
        String sheetName = request.getParameter("sheetName");
        String status = request.getParameter("status");

        // checks all params
        if (targetUsername == null || sheetName == null || status == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters: username, sheetName, or status");
            return;
        }

        try {
            // status of req to enum
            RequestStatus requestStatus = RequestStatus.valueOf(status.toUpperCase());

            // fining the correct rew for sheet and user
            PermissionRequest permissionRequest = findRequest(targetUsername, sheetName);

            if (permissionRequest != null) {
                // updating req
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

    // finds req according to user and sheet
    private PermissionRequest findRequest(String username, String sheetName) {
        return permissionManager.getPendingRequests().stream()
                .filter(req -> req.getRequesterUsername().equals(username) && req.getSheetName().equals(sheetName))
                .findFirst()
                .orElse(null);
    }
}
