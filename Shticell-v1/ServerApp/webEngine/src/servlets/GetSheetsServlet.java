package servlets;

import dto.SheetInfoDto;  // ייבוא ה-DTO
import utils.ServletUtils;
import utils.SessionUtils;
import users.UserManager;
import sheetEngine.SheetEngine;
import users.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

@WebServlet(name = "GetSheetsServlet", urlPatterns = {"/getSheets"})
public class GetSheetsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // שליפת ה-UserManager מה-context
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        // קריאה למתודה החדשה שמחזירה את כל המידע על הגיליונות
        List<SheetInfoDto> sheetList = userManager.getAllSheetsInfo();

        // המרת הרשימה ל-JSON ושליחתה ללקוח
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(sheetList);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }
}
