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
        // user manager from the context of session
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // gets version of list
        int sheetListVersion = userManager.getUpdateSheetListVersion();

        // sending the answer to client side
        response.setContentType("application/json");
        response.getWriter().write(String.valueOf(sheetListVersion));
    }
}
