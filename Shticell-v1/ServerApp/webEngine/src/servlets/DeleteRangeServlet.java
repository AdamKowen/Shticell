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

@WebServlet(name = "DeleteRangeServlet", urlPatterns = {"/deleteRange"})
public class DeleteRangeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // checks users login
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // range for deletion
        String rangeName = req.getParameter("rangeName");

        if (rangeName == null || rangeName.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid range name for deletion.");
            return;
        }

        // sheet engine of user
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        try {
            // deletes range
            sheetEngine.deleteRange(rangeName);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Range '" + rangeName + "' deleted successfully.");
        } catch (Exception e) {
            // error handling
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(e.getMessage());
        }
    }
}
