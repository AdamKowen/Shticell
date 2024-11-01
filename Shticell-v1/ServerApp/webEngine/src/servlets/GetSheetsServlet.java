package servlets;

import com.google.gson.Gson;
import dto.SheetInfoDto;  // ייבוא ה-DTO
import utils.ServletUtils;
import utils.SessionUtils;
import users.UserManager;
import permissions.PermissionManager;
import permissions.PermissionType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "GetSheetsServlet", urlPatterns = {"/getSheets"})
public class GetSheetsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // שליפת ה-UserManager וה-PermissionManager מה-context
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        PermissionManager permissionManager = userManager.getPermissionManager();

        // שליפת שם המשתמש מהסשן
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User is not logged in");
            return;
        }

        // שליפת כל המידע על הגיליונות
        List<SheetInfoDto> sheetList = userManager.getAllSheetsInfo();

        // עדכון הרשאות עבור כל גיליון ברשימה
        for (SheetInfoDto sheetInfo : sheetList) {
            String sheetName = sheetInfo.getSheetName();
            String ownerName = sheetInfo.getOwnerName();

            // בדיקה האם המשתמש הוא הבעלים של הגיליון
            if (ownerName.equals(username)) {
                sheetInfo.setAccess("owner");
            } else {
                // קבלת סוג ההרשאה למשתמש על הגיליון הנוכחי
                PermissionType permissionType = permissionManager.getPermissionType(sheetName, username);

                // קביעת ההרשאה לפי הסוג
                switch (permissionType) {
                    case WRITER:
                        sheetInfo.setAccess("write");
                        break;
                    case READER:
                        sheetInfo.setAccess("read");
                        break;
                    case NONE:
                    default:
                        sheetInfo.setAccess("no access");
                        break;
                }
            }
        }

        // המרת הרשימה ל-JSON ושליחתה ללקוח
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(sheetList);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }
}
