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

    //updating temp sheet for dynamic analysis function
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

        //getting new value from client
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
            // trying to update the value on the temp sheet
            sheetEngine.updateCellBasedOnSlider(cellId, newValueStr);

            // getting temp sheet dto
            SheetDto temporarySheetDto = sheetEngine.getTemporarySheetDTO();

            // sending to client the updated temp sheet
            String jsonResponse = JSONUtils.toJson(temporarySheetDto);
            response.getWriter().print(jsonResponse);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            // if a problem occurred update client side
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to update cell: " + e.getMessage());
        }
    }
}
