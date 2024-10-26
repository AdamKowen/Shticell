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
@WebServlet(name = "UpdateCellContentServlet", urlPatterns = {"/updateCellContent"})
public class UpdateCellContentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        String cellReference = req.getParameter("cellReference");
        String newContent = req.getParameter("newContent");

        if (cellReference == null || cellReference.isEmpty() || newContent == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Cell reference and new content are required");
            return;
        }

        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("User not found");
            return;
        }

        SheetEngine sheetEngine = user.getSheetEngine();

        try {
            // קריאה לפונקציה לעדכן את תוכן התא
            sheetEngine.updateCellValue(cellReference, newContent);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Cell content updated successfully");
        } catch (Exception e) {
            // טיפול בשגיאה והחזרת הודעת שגיאה ללקוח
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error updating cell content: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
