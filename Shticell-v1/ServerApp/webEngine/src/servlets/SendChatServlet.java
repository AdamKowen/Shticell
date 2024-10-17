package servlets;

import constants.Constants;
import utils.ServletUtils;
import utils.SessionUtils;
import chat.ChatManager;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "GetUserChatServlet", urlPatterns = {"/pages/chatroom/sendChat"})
public class SendChatServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        ChatManager chatManager = ServletUtils.getChatManager(getServletContext());
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        String userChatString = request.getParameter(Constants.CHAT_PARAMETER);
        if (userChatString != null && !userChatString.isEmpty()) {
            logServerMessage("Adding chat string from " + username + ": " + userChatString);
            synchronized (getServletContext()) {
                chatManager.addChatString(userChatString, username);
            }
        }
    }

    private void logServerMessage(String message) {
        System.out.println(message);
    }

}
