package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import loader.SheetLoadingException;
import org.xml.sax.SAXException;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;
import users.User;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

@WebServlet("/uploadSheet")  // URL endpoint to trigger the servlet
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Step 1: Extract the uploaded file from the request
        Part filePart = req.getPart("file");
        InputStream fileContent = filePart.getInputStream();

        // Step 2: Create a temporary file to save the uploaded XML content
        File tempFile = File.createTempFile("uploadedSheet", ".xml");
        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileContent.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        // Step 3: Retrieve the current user's SheetEngine from the session
        HttpSession session = req.getSession();
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            resp.getWriter().write("Error: No user found in session");
            return;
        }

        SheetEngine engine = currentUser.getSheetEngine();

        // Step 4: Load the sheet using the loadSheetFromXML method
        try {
            engine.loadSheetFromXML(tempFile.getAbsolutePath());
            resp.getWriter().write("Sheet loaded successfully");
        } catch (ParserConfigurationException | IOException | SAXException e) {
            resp.getWriter().write("Server Error loading the sheet: " + e.getMessage());
        } catch (SheetLoadingException e) {
            throw new RuntimeException(e);
        }

        // Step 5: Clean up the temporary file
        tempFile.delete();
    }
}

