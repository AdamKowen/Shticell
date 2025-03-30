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

@WebServlet(name = "CheckSheetVersionServlet", urlPatterns = {"/checkSheetVersion"})
public class CheckSheetVersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // gets username fron session
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // gets current version of users sheet (from client side)
        int clientSheetVersion;
        try {
            clientSheetVersion = Integer.parseInt(req.getParameter("sheetVersion"));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid sheet version");
            return;
        }

        // sheetengine of user
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        // checks if the curr version matches (client side) the one in the server
        int currentSheetVersion = sheetEngine.getCurrentSheetVersion();
        if (clientSheetVersion == currentSheetVersion) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Sheet version is up-to-date.");
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("Version conflict: Sheet has been updated. Please refresh.");
        }
    }
}
