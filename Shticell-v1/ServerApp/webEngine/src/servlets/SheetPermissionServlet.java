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


    // מקבל את כל ההרשאות והבקשות הממתינות עבור גיליון מסוים
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קבלת שם הגיליון מהפרמטר
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        PermissionManager permissionManager= userManager.getPermissionManager();
        Gson gson = new Gson();
        String sheetName = request.getParameter("sheetName");

        // בדיקת תקינות של הפרמטר
        if (sheetName == null || sheetName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing or empty sheetName parameter.");
            return;
        }

        // קבלת רשימת ההרשאות והבקשות מהמנהל
        List<PermissionDTO> permissionList = permissionManager.getPermissionsForSheetDTO(sheetName);

        // החזרת הנתונים כ-JSON
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(gson.toJson(permissionList));
    }
}

