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

@WebServlet(name = "ResetDynamicAnalysisServlet", urlPatterns = {"/resetDynamicAnalysis"})
public class ResetDynamicAnalysisServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // קבלת שם המשתמש מהסשן
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // קבלת ה-User וה-SheetEngine
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("User not found");
            return;
        }

        SheetEngine sheetEngine = user.getSheetEngine();

        // קריאה לפונקציה resetTempSheet
        sheetEngine.resetTempSheet();

        // החזרת תגובה מוצלחת
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("Dynamic analysis sliders reset successfully");
    }
}
