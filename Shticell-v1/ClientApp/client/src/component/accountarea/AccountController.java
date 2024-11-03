package component.accountarea;

import com.google.gson.Gson;
import component.api.AccountCommands;
import component.api.HttpStatusUpdate;
import component.chatarea.ChatAreaController;
import component.commands.CommandsController;
import component.main.AppMainController;
import component.sheetViewfinder.SheetViewfinderController;
import component.users.UsersListController;
import dto.PermissionDTO;
import dto.SheetInfoDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.input.MouseEvent;
import utils.SessionUtils;

import static util.Constants.REFRESH_RATE;

public class AccountController implements Closeable, HttpStatusUpdate, AccountCommands {

    public GridPane sheetCard;
    private AccountCommands accountCommands;
    @FXML private VBox usersListComponent;
    @FXML private UsersListController usersListComponentController;
    @FXML private VBox actionCommandsComponent;
    @FXML private CommandsController actionCommandsComponentController;
    @FXML private GridPane chatAreaComponent;
    @FXML private ChatAreaController chatAreaComponentController;


    private AppMainController chatAppMainController;

    private Timer timer;  // משתנה שיחזיק את ה-Timer
    private TimerTask sheetListRefresher;  // משתנה שיחזיק את ה-TimerTask



    @FXML
    private TableView<SheetInfoDto> sheetTableView;

    @FXML
    private TableColumn<SheetInfoDto, String> nameColumn;  // עמודת שם הגיליון

    @FXML
    private TableColumn<SheetInfoDto, Integer> rowsColumn;  // עמודת שורות

    @FXML
    private TableColumn<SheetInfoDto, Integer> columnsColumn;  // עמודת עמודות

    @FXML
    private TableColumn<SheetInfoDto, String> ownerColumn;  // עמודת שם הבעלים

    @FXML
    private TableColumn<SheetInfoDto, String> accessColumn;  // עמודת הרשאות


    @FXML
    private Label selectedSheetNameLabel;  // תווית שם הגיליון שנבחר


    private String selectedSheetName = null;  // משתנה לשמירת שם הגיליון הנבחר

    private String selectedUsername = null;  // משתנה לשמירת שם המשתמש שנבחר







    @FXML
    private TableView<PermissionDTO> sheetPremmisionTable;

    @FXML
    private TableColumn<PermissionDTO, String> permissionUserCol;  // עמודת שם המשתמש בטבלת ההרשאות, כ-String

    @FXML
    private TableColumn<PermissionDTO, String> permissionCol;  // עמודת סוג ההרשאה בטבלת ההרשאות, כ-String

    @FXML
    private TableColumn<PermissionDTO, String> statusCol;  // עמודת סטטוס בטבלת ההרשאות, כ-String

    @FXML
    private Label premmisionStatus;  // תווית סטטוס ההרשאה עבור המשתמש

    @FXML
    private Button acceptButton;  // כפתור לאישור הרשאה

    @FXML
    private Button rejectButton;  // כפתור לדחיית הרשאה

    @FXML
    private Button openSheetViewfinder;  // כפתור לדחיית הרשאה

    @FXML
    private Label selectedUserName;  // תווית שם המשתמש הנבחר בטבלת ההרשאות

    @FXML
    public void initialize() {
        usersListComponentController.setHttpStatusUpdate(this);
        accountCommands = this;
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        initTableColumns();  // אתחול העמודות בעת ההפעלה
        //startSheetListRefresher();   // התחלת עדכון הטבלה כל 2 שניות (REFRESH_RATE)



        chatAreaComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        chatAreaComponentController.startListRefresher();


        permissionUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionCol.setCellValueFactory(new PropertyValueFactory<>("permission"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));



// מאזין ללחיצה על שורה בטבלה
        sheetTableView.setOnMouseClicked((MouseEvent event) -> {
            SheetInfoDto selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
            if (selectedSheet != null) {
                selectedSheetName = selectedSheet.getSheetName();  // שמירת שם הגיליון הנבחר
                System.out.println("Selected sheet: " + selectedSheetName);  // הדפסת שם הגיליון שנבחר

                // עדכון תווית שם הגיליון בכרטיסייה
                selectedSheetNameLabel.setText(selectedSheetName);

                // בקשת הרשאות למשתמש הנוכחי (נניח שיש לנו פונקציה מתאימה שמחזירה את סוג ההרשאה)
                String userAccess = selectedSheet.getAccess();

                // עדכון מצב הכפתור של צפייה בגיליון
                openSheetViewfinder.setDisable(!("owner".equals(userAccess) || "write".equals(userAccess) || "read".equals(userAccess)));

                // קביעה מה להציג בהתאם לסוג ההרשאה
                switch (userAccess) {
                    case "owner": // בעל הגיליון
                        sheetPremmisionTable.setVisible(true);  // הצגת הטבלה
                        statusCol.setVisible(true);             // הצגת עמודת הסטטוס
                        acceptButton.setDisable(true);          // כפתורי אישור ודחייה מושבתים עד לבחירה של בקשה
                        rejectButton.setDisable(true);
                        loadPermissionsForOwner(selectedSheet.getSheetName());
                        break;

                    case "write": // הרשאת כתיבה
                    case "read":  // הרשאת קריאה
                        sheetPremmisionTable.setVisible(true);  // הצגת הטבלה
                        statusCol.setVisible(false);            // הסתרת עמודת הסטטוס
                        acceptButton.setDisable(true);          // כפתורי אישור ודחייה מושבתים עבור משתמשים שאינם הבעלים
                        rejectButton.setDisable(true);
                        loadPermissionsForEditorOrViewer(selectedSheet.getSheetName());
                        break;

                    case "no access":  // אין הרשאה
                        sheetPremmisionTable.setVisible(false); // הסתרת הטבלה כולה
                        premmisionStatus.setText("בקשה ממתינה לאישור");
                        openSheetViewfinder.setDisable(true);   // השבתת הכפתור לצפייה בגיליון
                        break;
                }

                // הסרת הסימון לאחר הצגת המידע
                Platform.runLater(() -> sheetTableView.getSelectionModel().clearSelection());
            }
        });

        sheetPremmisionTable.setOnMouseClicked((MouseEvent event) -> {
            PermissionDTO selectedPermission = sheetPremmisionTable.getSelectionModel().getSelectedItem();
            if (selectedPermission != null) {
                selectedUserName.setText(selectedPermission.getUsername());  // הצגת שם המשתמש שנבחר
                selectedUsername = selectedPermission.getUsername();  // שמירת שם המשתמש שנבחר

                // קביעת האם הכפתורים יושבתו בהתאם לסטטוס
                boolean isPending = "Pending".equals(selectedPermission.getStatus());
                acceptButton.setDisable(!isPending);
                rejectButton.setDisable(!isPending);
            } else {
                selectedUsername = null;  // איפוס המשתנה אם אין משתמש מסומן או שאין הרשאות
                acceptButton.setDisable(true);
                rejectButton.setDisable(true);
            }
        });

    }


    @FXML
    private void requestPermissionAccess(String permissionType) {
        if (selectedSheetName != null) {
            String url = Constants.PERMISSION_REQUEST_URL;
            RequestBody body = new FormBody.Builder()
                    .add("sheetName", selectedSheetName)
                    .add("type", permissionType)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> updateHttpLine("Error sending " + permissionType + " request: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Platform.runLater(() -> updateHttpLine(permissionType + " request response: " + response.message()));
                    response.close();
                }
            });
        }
    }

    // קריאה לפונקציה עבור סוגי בקשות שונים:
    @FXML
    private void requestWriterAccess() {
        requestPermissionAccess("WRITER");
    }

    @FXML
    private void requestReaderAccess() {
        requestPermissionAccess("READER");
    }
    @FXML
    private void acceptRequest() {
        if (selectedSheetName != null && selectedUsername != null) {
            String url = Constants.APPROVAL_REQUEST_URL; // נתיב לסרבלט החדש
            System.out.println("URL to Servlet: " + url);
            System.out.println("Selected Sheet Name: " + selectedSheetName);
            System.out.println("Selected Username: " + selectedUsername);

            RequestBody body = new FormBody.Builder()
                    .add("username", selectedUsername)
                    .add("sheetName", selectedSheetName)
                    .add("status", "APPROVED")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body) // שימוש ב-POST עבור בקשה פשוטה
                    .build();

            System.out.println("Sending HTTP request...");

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        String errorMessage = "Error accepting request: " + e.getMessage();
                        System.out.println(errorMessage);
                        updateHttpLine(errorMessage);
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    Platform.runLater(() -> {
                        System.out.println("Response Code: " + response.code());
                        System.out.println("Response Message: " + response.message());
                        System.out.println("Response Body: " + responseBody);
                        updateHttpLine("Request accepted: " + response.message());
                    });
                    response.close();
                }
            });
        } else {
            System.out.println("Either selectedSheetName or selectedUsername is null");
        }
    }


    @FXML
    private void rejectRequest() {
        if (selectedSheetName != null && selectedUsername != null) {
            String url = Constants.APPROVAL_REQUEST_URL; // נתיב לסרבלט החדש
            System.out.println("URL to Servlet: " + url);
            System.out.println("Selected Sheet Name: " + selectedSheetName);
            System.out.println("Selected Username: " + selectedUsername);

            RequestBody body = new FormBody.Builder()
                    .add("username", selectedUsername)
                    .add("sheetName", selectedSheetName)
                    .add("status", "DENIED")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body) // שימוש ב-POST עבור בקשה פשוטה
                    .build();

            System.out.println("Sending HTTP request...");

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        String errorMessage = "Error rejecting request: " + e.getMessage();
                        System.out.println(errorMessage);
                        updateHttpLine(errorMessage);
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    Platform.runLater(() -> {
                        System.out.println("Response Code: " + response.code());
                        System.out.println("Response Message: " + response.message());
                        System.out.println("Response Body: " + responseBody);
                        updateHttpLine("Request rejected: " + response.message());
                    });
                    response.close();
                }
            });
        } else {
            System.out.println("Either selectedSheetName or selectedUsername is null");
        }
    }




    @FXML
    void logoutClicked(ActionEvent event) {
        accountCommands.updateHttpLine(Constants.LOGOUT);
        HttpClientUtil.runAsync(Constants.LOGOUT, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                accountCommands.updateHttpLine("Logout request ended with failure...:(");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() || response.isRedirect()) {
                    HttpClientUtil.removeCookiesOf(Constants.BASE_DOMAIN);
                    accountCommands.logout();
                }
            }
        });
    }


    @Override
    public void updateHttpLine(String line) {
        chatAppMainController.updateHttpLine(line);
    }

    @Override
    public void close() throws IOException {
        usersListComponentController.close();
    }

    public void setActive() {
        usersListComponentController.startListRefresher();
        startSheetListRefresher();  // התחלת עדכון הטבלה כאן
    }

    public void setInActive() {
        try {
            usersListComponentController.close();
            stopSheetListRefresher();  // הפסקת העדכונים האוטומטיים
        } catch (Exception ignored) {}
    }

    public void setChatAppMainController(AppMainController chatAppMainController) {
        this.chatAppMainController = chatAppMainController;
    }

    @Override
    public void logout() {
        chatAppMainController.switchToLogin();
    }




    @FXML
    void loadSheetClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");

        // Adding a filter to allow only XML files (optional)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // שליחת הקובץ לשרת
                uploadFileToServer(file);
            } catch (IOException e) {
                e.printStackTrace();
                // כאן אפשר להוסיף טיפול בשגיאות כמו הצגת הודעה למשתמש
                System.out.println("Error occurred during file upload.");
            }
        }else {
            // Display an error message if no file is selected
            //fileNameLabel.setText("No file selected or an error occurred.");
        }
    }




    @FXML
    // פעולה לפתיחת ה-Viewfinder
    public void openSheetViewfinder() {
        if (selectedSheetName != null) {
            chatAppMainController.switchToViewfinder(selectedSheetName);  // מעבר ל-viewfinder
        } else {
            System.out.println("No sheet selected");
        }
    }



/*
    private void uploadFileToServer(File file) throws IOException {
        // Define the URL of the server to which the file will be uploaded
        String RESOURCE = "/uploadSheet";  // The path where the file should be uploaded
        String BASE_URL = "http://localhost:8080/webEngine_Web_exploded";  // Base URL of your application

        // Create a multipart request body for the file upload
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("text/plain")))  // Media type of the file (adjust if needed)
                .build();

        // Create an HTTP request to send the file
        Request request = new Request.Builder()
                .url(BASE_URL + RESOURCE)  // Full URL of the server endpoint
                .post(body)  // POST method for file upload
                .build();

        // Use OkHttpClient to execute the request
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            // Check if the response is successful
            if (response.isSuccessful()) {
                System.out.println("File uploaded successfully: " + response.body().string());
            } else {
                System.out.println("Failed to upload file. Response code: " + response.code()+response.body().string());
            }
        } catch (IOException e) {
            System.out.println("Error during file upload: " + e.getMessage());
            throw e;  // Rethrow the exception to be handled by the caller
        }
    }

 */

    private void uploadFileToServer(File file) throws IOException {
        String RESOURCE = "/uploadSheet";
        String BASE_URL = "http://localhost:8080/webEngine_Web_exploded";
        String finalUrl = BASE_URL + RESOURCE;  // משתמש באותו BASE_URL כמו שאר הקוד

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/xml")))
                .build();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)  // משתמש ב-POST במקום GET
                .build();

        // משתמש באותו HTTP_CLIENT עם אותו CookieManager
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        updateHttpLine("Error during file upload: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                Platform.runLater(() -> {
                    if (response.isSuccessful()) {
                        updateHttpLine("Success: " + responseBody);
                    } else {
                        updateHttpLine("Error: " + responseBody + " (Code: " + response.code() + ")");
                    }
                });
            }
        });
    }




    public void setAccountCommands(AccountCommands chatRoomMainController) {
        this.accountCommands = chatRoomMainController;
    }



    private void initTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        rowsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfRows"));
        columnsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfColumns"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        accessColumn.setCellValueFactory(new PropertyValueFactory<>("access"));  // קישור עמודת הרשאות לשדה access
    }


    // הפונקציה שמתחילה את הרענון המחזורי של רשימת הגיליונות
    private void startSheetListRefresher() {
        sheetListRefresher = new component.accountarea.SheetListRefresher(
                this::logHttpRequest,
                this::updateSheetTable);
        timer = new Timer();
        timer.schedule(sheetListRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    // עדכון הטבלה עם הנתונים החדשים
    private void updateSheetTable(List<SheetInfoDto> sheetInfoDtoList) {
        Platform.runLater(() -> {
            ObservableList<SheetInfoDto> data = FXCollections.observableArrayList(sheetInfoDtoList);
            sheetTableView.setItems(data);
        });
    }

    // לוג הבקשה לשרת
    private void logHttpRequest(String logMessage) {
        System.out.println(logMessage);  // יכול לשמש למעקב אחר בקשות
    }



    private void displaySheetDetails(SheetInfoDto selectedSheet) {
        // הצגת שם הגיליון הנבחר
        selectedSheetNameLabel.setText(selectedSheet.getSheetName());

        // בדיקה אם המשתמש הוא הבעלים של הגיליון
        if (selectedSheet.getAccess().equals("owner")) {
            // אם המשתמש הוא הבעלים - מציגים את כל המשתתפים כולל פנדינג
            loadPermissionsForOwner(selectedSheet.getSheetName());
        } else {
            // אם המשתמש הוא בעל הרשאת עריכה או קריאה
            if ("write".equals(selectedSheet.getAccess()) || "read".equals(selectedSheet.getAccess())) {
                loadPermissionsForEditorOrViewer(selectedSheet.getSheetName());
            } else {
                // אם אין למשתמש הרשאה
                sheetPremmisionTable.setVisible(false);
                //checkPendingRequestStatus(selectedSheet.getSheetName());
            }
        }
    }


    private void stopSheetListRefresher() {
        if (sheetListRefresher != null && timer != null) {
            sheetListRefresher.cancel();
            timer.cancel();
            timer.purge();
            sheetListRefresher = null;
            timer = null;
        }
    }

    private void loadPermissionsForOwner(String sheetName) {
        // URL של ה-Servlet כולל שם הגיליון כפרמטר
        String url = Constants.SHEET_PERMISSION_URL + sheetName;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // ביצוע בקשה אסינכרונית
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> updateHttpLine("Error fetching permissions for owner: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<PermissionDTO>>() {}.getType();
                    List<PermissionDTO> permissionList = gson.fromJson(responseBody, listType);

                    Platform.runLater(() -> {
                        ObservableList<PermissionDTO> permissions = FXCollections.observableArrayList(permissionList);
                        sheetPremmisionTable.setItems(permissions);
                        statusCol.setVisible(true); // הצגת עמודת הסטטוס לבעלים
                    });
                } else {
                    Platform.runLater(() -> updateHttpLine("Error fetching permissions for owner: " + response.code()));
                }
                response.close();  // סגירת ה-Response למניעת דליפות
            }
        });
    }



    private void loadPermissionsForEditorOrViewer(String sheetName) {
        String url = Constants.SHEET_PERMISSION_URL + sheetName;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> updateHttpLine("Error fetching permissions for viewer/editor: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<PermissionDTO>>() {}.getType();
                    List<PermissionDTO> permissionList = gson.fromJson(responseBody, listType);

                    Platform.runLater(() -> {
                        ObservableList<PermissionDTO> approvedPermissions = FXCollections.observableArrayList();
                        for (PermissionDTO permission : permissionList) {
                            if (!"pending".equals(permission.getStatus())) {
                                approvedPermissions.add(permission);
                            }
                        }
                        sheetPremmisionTable.setItems(approvedPermissions);
                        statusCol.setVisible(false); // הסתרת עמודת הסטטוס
                    });
                } else {
                    Platform.runLater(() -> updateHttpLine("Error fetching permissions for viewer/editor: " + response.code()));
                }
                response.close();  // סגירת ה-Response למניעת דליפות
            }
        });
    }




}




