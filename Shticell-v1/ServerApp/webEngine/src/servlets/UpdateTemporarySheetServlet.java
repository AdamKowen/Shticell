package servlets;

import com.google.gson.Gson;
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
@WebServlet(name = "UpdateTemporarySheetServlet", urlPatterns = {"/updateTemporarySheet"})
public class UpdateTemporarySheetServlet extends HttpServlet {

    @Override
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: User not logged in");
            return;
        }

        String cellId = request.getParameter("cellId");
        String newValueStr = request.getParameter("newValue");

        if (cellId == null || newValueStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing cellId or newValue parameter");
            return;
        }

        double newValue;
        try {
            newValue = Double.parseDouble(newValueStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid newValue: must be a numeric string");
            return;
        }

        User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
        SheetEngine sheetEngine = user.getSheetEngine();

        try {
            // מנסה לעדכן את הערך בגליון הזמני
            sheetEngine.updateCellBasedOnSlider(cellId, newValueStr);

            // קבלת הגיליון הזמני כ-DTO
            SheetDto temporarySheetDto = sheetEngine.getTemporarySheetDTO();

            // המרה ל-JSON ושליחה ללקוח
            String jsonResponse = JSONUtils.toJson(temporarySheetDto);
            response.getWriter().print(jsonResponse);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            // במקרה של שגיאה בעדכון התא, החזרה ללקוח עם הודעת שגיאה מתאימה
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to update cell: " + e.getMessage());
        }
    }
}
