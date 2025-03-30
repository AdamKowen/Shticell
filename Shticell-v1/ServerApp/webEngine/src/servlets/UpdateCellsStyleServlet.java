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
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@WebServlet(name = "UpdateCellsStyleServlet", urlPatterns = {"/updateCellsStyle"})
public class UpdateCellsStyleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // params for request
        String styleType = req.getParameter("styleType"); // "backgroundColor", "textColor", "alignment"
        String styleValue = req.getParameter("styleValue");

        // checks if everything was received
        if (styleType == null || styleValue == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Missing parameters");
            return;
        }

        // lists of cols and rows as jason
        Gson gson = new Gson();
        List<String> columns = gson.fromJson(req.getParameter("columns"), new TypeToken<List<String>>(){}.getType());
        List<Integer> rows = gson.fromJson(req.getParameter("rows"), new TypeToken<List<Integer>>(){}.getType());

        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        try {
            // update style of every cell in lists
            sheetEngine.updateCellsStyle(columns, rows, styleType, styleValue);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Style updated successfully for cell range.");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Failed to update style: " + e.getMessage());
        }
    }
}
