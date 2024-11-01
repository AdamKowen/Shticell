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
import java.io.*;

@WebServlet("/uploadSheet")  // URL endpoint to trigger the servlet
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        /*
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
        HttpSession session = req.getSession(false);
       String username = SessionUtils.getUsername(req);



       resp.getWriter().write("connected username:"+username);
       User currentUser = (User) session.getAttribute("user");


        SheetEngine engine = new SheetEngineImpl();//currentUser.getSheetEngine();

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

         */



        // First, check session
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

        // Process file upload
        try {
            // Get the uploaded file part
            Part filePart = req.getPart("file");
            if (filePart == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("No file uploaded");
                return;
            }

            // Verify file name and extension
            String fileName = getSubmittedFileName(filePart);
            if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Invalid file type. Only XML files are allowed.");
                return;
            }

            // Create a temporary file with a unique name
            String tempDir = System.getProperty("java.io.tmpdir");
            String tempFileName = "sheet_" + username + "_" + System.currentTimeMillis() + ".xml";
            File tempFile = new File(tempDir, tempFileName);

            // Save uploaded file to temp location
            try (InputStream input = filePart.getInputStream();
                 FileOutputStream output = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[8192]; // 8KB buffer
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }

            // Get user's SheetEngine and load the file
            SheetEngine engine = currentUser.getSheetEngine();
            if (engine == null) {
                engine = new SheetEngineImpl();
                currentUser.setSheetEngine(engine);
            }

            try {
                // Load the sheet from the temporary file
                engine.loadSheetFromXML(tempFile.getAbsolutePath());
                SheetPermission newPerm=new SheetPermission(username, PermissionType.OWNER);
                userManager.getPermissionManager().addPermission(engine.getCurrentSheetDTO().getName(),newPerm);

                // Update response
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("Sheet uploaded and loaded successfully for user: " + username);
                resp.getWriter().write(" Permission added for user: " + username+" as Owner");




            } catch (ParserConfigurationException | SAXException | SheetLoadingException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("Error parsing XML file: " + e.getMessage());
            } finally {
                // Clean up: delete temporary file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error processing file: File size too large");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Server error processing file: " + e.getMessage());
        }

    }



    // Helper method to get the original file name from Part
    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            // Parse the content-disposition header to get the original file name
            for (String cd : contentDisp.split(";")) {
                if (cd.trim().startsWith("filename")) {
                    return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }
}

