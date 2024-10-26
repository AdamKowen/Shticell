package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import dto.SheetDto;  // ייבוא ה-DTO
import sheetEngine.SheetEngine;
import users.User;
import utils.JSONUtils;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;
@WebServlet(name = "GetCurrentSheetServlet", urlPatterns = {"/getCurrentSheet"})
public class GetCurrentSheetServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Set the response type to JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Get the username from the session
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        // Get the SheetEngine from the user
        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        // Get the current sheet as DTO
        SheetDto sheetDto = sheetEngine.getCurrentSheetDTO();

        // Convert the SheetDto to JSON using JSONUtils
        String jsonResponse = JSONUtils.toJson(sheetDto);

        // Write the JSON response back to the client
        PrintWriter out = resp.getWriter();
        out.print(jsonResponse);
        out.flush();
    }
}



/*
    private SheetEngine sheetEngine = new SheetEngineImpl();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)

            throws ServletException, IOException {
        // מחזיר את מצב הגיליון הנוכחי כ-JSON
        String sheetJson = JSONUtils.toJson(sheetEngine.getCurrentSheetDTO());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(sheetJson);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // קריאת נתוני התא לעדכון
        String cellId = request.getParameter("cellId");
        String newValue = request.getParameter("newValue");

        try {
            // עדכון תא בגיליון
            sheetEngine.updateCellValue(cellId, newValue);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Cell updated successfully.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Failed to update cell: " + e.getMessage());
        }
    }

     */

