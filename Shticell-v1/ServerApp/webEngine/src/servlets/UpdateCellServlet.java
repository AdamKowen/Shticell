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

@WebServlet(name = "UpdateCellServlet", urlPatterns = {"/updateCell"})
public class UpdateCellServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get the username from session
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // Get parameters from request
        String cellCoordinate = req.getParameter("coordinate"); // לדוגמה: "A1"
        String newValue = req.getParameter("newValue");
        int clientSheetVersion;

        try {
            clientSheetVersion = Integer.parseInt(req.getParameter("sheetVersion"));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid sheet version");
            return;
        }

        // Get the user's SheetEngine
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        // Check if the sheet version matches
        int currentSheetVersion = sheetEngine.getCurrentSheetVersion();
        if (clientSheetVersion != currentSheetVersion) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("Version conflict: Sheet has been updated. Please refresh and try again.");
            return;
        }

        // Try to update the cell
        try {
            sheetEngine.updateCellValue(cellCoordinate, newValue);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Cell updated successfully.");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Failed to update cell: " + e.getMessage());
        }
    }
}
