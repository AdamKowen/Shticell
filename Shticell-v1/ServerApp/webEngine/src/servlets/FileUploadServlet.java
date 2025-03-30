package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import permissions.PermissionType;
import permissions.SheetPermission;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;
import users.User;
import users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

@WebServlet("/uploadSheet")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = SessionUtils.getUsername(req);
        if (username == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("User not logged in");
            return;
        }

        // Get UserManager and find the user
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        User currentUser = userManager.getUser(username);

        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("User not found");
            return;
        }

        try {
            // Get the uploaded file part
            Part filePart = req.getPart("file");
            if (filePart == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("No file uploaded");
                return;
            }

            // Verify file type
            String fileName = getSubmittedFileName(filePart);
            if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid file type. Only XML files are allowed.");
                return;
            }

            // Load the sheet from the InputStream directly
            SheetEngine engine = currentUser.getSheetEngine();
            if (engine == null) {
                engine = new SheetEngineImpl();
                currentUser.setSheetEngine(engine);
            }

            try (InputStream input = filePart.getInputStream()) {
                engine.loadSheetFromXML(input);
                SheetPermission newPerm = new SheetPermission(username, PermissionType.OWNER);
                userManager.getPermissionManager().addPermission(engine.getCurrentSheetDTO().getName(), newPerm);

                // Update response
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Sheet uploaded and loaded successfully for user: " + username);
                resp.getWriter().write(" Permission added for user: " + username + " as Owner");
                userManager.updateSheetListVersion();
            } catch (ParserConfigurationException | SAXException | SheetLoadingException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Error parsing XML file: " + e.getMessage());
            }

        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error processing file: File size too large");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Problem processing file: " + e.getMessage());
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String cd : contentDisp.split(";")) {
                if (cd.trim().startsWith("filename")) {
                    return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }
}