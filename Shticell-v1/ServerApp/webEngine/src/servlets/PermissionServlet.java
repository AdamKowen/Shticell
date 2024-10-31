package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permissions.PermissionType;
import permissions.SheetPermission;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/permissions/*")
public class PermissionServlet extends HttpServlet {

    UserManager userManager = ServletUtils.getUserManager(getServletContext());
    private final Gson gson = new Gson();

    // הוספת הרשאה חדשה
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        String username = request.getParameter("username");
        String type = request.getParameter("type");

        if (sheetName == null || username == null || type == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing parameters.");
            return;
        }

        PermissionType permissionType = PermissionType.valueOf(type.toUpperCase());
        userManager.addPermission(sheetName, username, permissionType);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Permission added successfully.");
    }

    // עדכון הרשאה קיימת/
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] pathInfo = request.getPathInfo().split("/");
        if (pathInfo.length < 3) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid request.");
            return;
        }

        String sheetName = pathInfo[1];
        String username = pathInfo[2];
        String newType = request.getParameter("newType");

        if (newType == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing newType parameter.");
            return;
        }

        PermissionType permissionType = PermissionType.valueOf(newType.toUpperCase());
        userManager.updatePermission(sheetName, username, permissionType);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("Permission updated successfully.");
    }

    // קבלת כל ההרשאות לגיליון מסוים
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] pathInfo = request.getPathInfo().split("/");
        if (pathInfo.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid request.");
            return;
        }

        String sheetName = pathInfo[1];
        List<SheetPermission> permissions = userManager.getPermissionManager().getPermissionsForSheet(sheetName);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(gson.toJson(permissions));
    }


}