package servlets;

import com.google.gson.Gson;
import dto.PermissionDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import permissions.PermissionManager;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/sheet-permissions")
public class SheetPermissionServlet extends HttpServlet {


    // all permission of a certain sheet
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // getting sheet name from params
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        PermissionManager permissionManager= userManager.getPermissionManager();
        Gson gson = new Gson();
        String sheetName = request.getParameter("sheetName");

        // checks if valid params
        if (sheetName == null || sheetName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing or empty sheetName parameter.");
            return;
        }

        /// permission list from manager
        List<PermissionDTO> permissionList = permissionManager.getPermissionsForSheetDTO(sheetName);

        // return as jason
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(gson.toJson(permissionList));
    }
}

