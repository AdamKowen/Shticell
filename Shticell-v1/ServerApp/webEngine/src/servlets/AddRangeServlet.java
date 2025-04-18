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

@WebServlet(name = "AddRangeServlet", urlPatterns = {"/addRange"})
public class AddRangeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // checks users login information / session
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // gets params from request
        String rangeName = req.getParameter("rangeName");
        String topLeft = req.getParameter("topLeft");
        String bottomRight = req.getParameter("bottomRight");

        if (rangeName == null || topLeft == null || bottomRight == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid parameters for adding range.");
            return;
        }

        // gets sheet engine of user
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        try {
            // adding the range
            sheetEngine.addRange(rangeName, topLeft, bottomRight);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Range '" + rangeName + "' added successfully.");
        } catch (Exception e) {
            // error handeling
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(e.getMessage());
        }
    }
}
