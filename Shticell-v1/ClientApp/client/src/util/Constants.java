package util;

import com.google.gson.Gson;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 250;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/component/main/app-main.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/component/login/login.fxml";
    public final static String CHAT_ROOM_FXML_RESOURCE_LOCATION = "/component/chatroom/chat-room-main.fxml";
    public final static String ACCOUNT_AREA_FXML_RESOURCE_LOCATION = "/component/accountarea/account-area.fxml";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/shticell";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;


    //server functions:
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/loginShortResponse";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/logout";

    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/pages/chatroom/sendChat";
    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";

    public final static String SHEET_LIST = FULL_SERVER_PATH + "/getSheets";
    public final static String SHEET_URL = FULL_SERVER_PATH + "/getCurrentSheet";
    public final static String SET_SHEET_URL = FULL_SERVER_PATH + "/setCurrentSheet";
    public final static String GET_SHEET_VERSION_URL = FULL_SERVER_PATH + "/getSheetVersion";
    public final static String VERSION_CHECK_URL = FULL_SERVER_PATH + "/checkSheetVersion";


    public final static String UPDATE_CELL_URL = FULL_SERVER_PATH + "/updateCell";
    public final static String UPDATE_CELLS_STYLE_URL = FULL_SERVER_PATH + "/updateCellsStyle";


    public final static String DELETE_RANGE_URL = FULL_SERVER_PATH + "/deleteRange";
    public final static String ADD_RANGE_URL = FULL_SERVER_PATH + "/addRange";

    public final static String UPDATE_TEMP_SHEET_URL = FULL_SERVER_PATH + "/updateTemporarySheet";
    public final static String SHEET_PERMISSION_URL = FULL_SERVER_PATH + "/sheet-permissions?sheetName=";
    public final static String PERMISSION_REQUEST_URL = FULL_SERVER_PATH + "/permission-requests";
    public final static String APPROVAL_REQUEST_URL = FULL_SERVER_PATH + "/approval-request";
    public final static String REQUEST_URL = FULL_SERVER_PATH + "/userPendingRequestStatus";


    public final static String UPLOAD_SHEET_URL = FULL_SERVER_PATH + "/uploadSheet";

    public final static String SHEET_LIST_VERSION = FULL_SERVER_PATH + "/getSheetListVersion";

    public final static String RESET_DYNAMIC_ANALYSIS_URL = FULL_SERVER_PATH + "/resetDynamicAnalysis";




    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();
}
