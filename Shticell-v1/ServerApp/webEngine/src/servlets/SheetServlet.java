package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;
import utils.JSONUtils;

import java.io.IOException;

@WebServlet("/sheet")
public class SheetServlet extends HttpServlet {

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
}

