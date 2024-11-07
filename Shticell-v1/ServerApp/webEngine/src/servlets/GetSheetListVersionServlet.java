package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ServletUtils;
import users.UserManager;

import java.io.IOException;

@WebServlet(name = "GetSheetListVersionServlet", urlPatterns = {"/getSheetListVersion"})
public class GetSheetListVersionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // שליפת ה-UserManager מה-context
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // קבלת מספר הגרסה העדכני של רשימת הגיליונות
        int sheetListVersion = userManager.getUpdateSheetListVersion();

        // שליחה כתגובה ללקוח
        response.setContentType("application/json");
        response.getWriter().write(String.valueOf(sheetListVersion));
    }
}
