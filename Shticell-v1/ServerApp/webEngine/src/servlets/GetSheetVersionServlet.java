package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import dto.SheetDto;
import sheetEngine.SheetEngine;
import users.User;
import utils.JSONUtils;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "GetSheetVersionServlet", urlPatterns = {"/getSheetVersion"})
public class GetSheetVersionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // קביעת סוג התוכן ל-JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // קבלת שם המשתמש מה-Session
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // קבלת מספר הגרסה מהבקשה
        String versionParam = req.getParameter("version");
        if (versionParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Version parameter is required");
            return;
        }

        int version;
        try {
            version = Integer.parseInt(versionParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid version number");
            return;
        }

        // קבלת ה-SheetEngine של המשתמש
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        // קבלת גרסה מסוימת כ-DTO
        SheetDto sheetVersionDto = sheetEngine.getVersionDto(version);
        if (sheetVersionDto == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Version not found");
            return;
        }

        // המרת ה-SheetDto ל-JSON באמצעות JSONUtils
        String jsonResponse = JSONUtils.toJson(sheetVersionDto);

        // שליחת התגובה בחזרה ללקוח
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse);
        out.flush();
    }
}
