package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import sheetEngine.SheetEngine;
import users.User;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet(name = "SetCurrentSheetServlet", urlPatterns = {"/setCurrentSheet"})
public class SetCurrentSheetServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }


        String sheetName = req.getParameter("sheetName");
        if (sheetName == null || sheetName.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Sheet name is required");
            return;
        }

        // Get the user and their sheet engine
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();


        // Try to set the current sheet
        boolean success = sheetEngine.setCurrentSheet(sheetName);
        if (success) {
            resp.getWriter().write("Current sheet set to: " + sheetName);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Sheet not found");
        }

        // Set the current sheet in the engine
        resp.getWriter().write("Current sheet set to: " + sheetName);
    }
}

