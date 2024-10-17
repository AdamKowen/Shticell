package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import sheetEngine.SheetEngine;
import sheetEngine.SheetEngineImpl;

import java.io.IOException;
import java.io.InputStream;

@WebServlet("/uploadSheet")
public class FileUploadServlet extends HttpServlet {

    private SheetEngine sheetEngine = new SheetEngineImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Part filePart = request.getPart("file"); // קובץ XML מהקליינט
        InputStream fileContent = filePart.getInputStream();

        try {
            // קריאה וטעינת הגיליון ממנוע ה-SheetEngine
            //sheetEngine.loadSheetFromXML(fileContent);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("File uploaded successfully and loaded into the sheet.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Failed to load sheet: " + e.getMessage());
        }
    }
}

