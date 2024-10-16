package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import users.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private Map<String, User> users = new HashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // קריאת שם המשתמש מהבקשה
        String username = request.getParameter("username");

        // בדיקת תקינות השם והחזרת session ID
        if (users.containsKey(username)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Username already exists.");
        } else {
            User user = new User(username);
            users.put(username, user);
            // חזרה עם ID של הסשן
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("User logged in successfully.");
        }
    }
}

