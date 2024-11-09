package component.accountarea;

import com.google.gson.Gson;
import component.api.AccountCommands;
import component.api.HttpStatusUpdate;
import component.chatarea.ChatAreaController;
import component.commands.CommandsController;
import component.main.AppMainController;
import component.users.UsersListController;
import dto.PermissionDTO;
import dto.SheetInfoDto;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import javafx.scene.input.MouseEvent;

import static util.Constants.*;

public class AccountController implements Closeable, HttpStatusUpdate, AccountCommands {


    private boolean readerUser = false;
    public GridPane sheetCard;
    private AccountCommands accountCommands;
    @FXML private VBox usersListComponent;
    @FXML private UsersListController usersListComponentController;
    @FXML private VBox actionCommandsComponent;
    @FXML private CommandsController actionCommandsComponentController;
    @FXML private GridPane chatAreaComponent;
    @FXML private ChatAreaController chatAreaComponentController;
    private List<PermissionDTO> currentPermissions = new ArrayList<>();

    @FXML
    private ToggleButton darkModeToggle; // כפתור להחלפת מצב dark mode

    @FXML
    BorderPane mainPane;

    private AppMainController chatAppMainController;

    private Timer timer;  // משתנה שיחזיק את ה-Timer
    private TimerTask sheetListRefresher;  // משתנה שיחזיק את ה-TimerTask
    private Timer permissionTimer;
    private TimerTask permissionListRefresher;


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
    private Label labelForStatus;

    @FXML
    private Label selectedSheetNameLabel;  // תווית שם הגיליון שנבחר


    @FXML
    private Label loadingStatusLabel;

    private String selectedSheetName = null;  // משתנה לשמירת שם הגיליון הנבחר

    private String selectedUsername = null;  // משתנה לשמירת שם המשתמש שנבחר



    private AppMainController appMainController;



    @FXML
    private TableView<PermissionDTO> sheetPremmisionTable;

    @FXML
    private TableColumn<PermissionDTO, String> permissionUserCol;  // עמודת שם המשתמש בטבלת ההרשאות, כ-String

    @FXML
    private TableColumn<PermissionDTO, String> permissionCol;  // עמודת סוג ההרשאה בטבלת ההרשאות, כ-String

    @FXML
    private TableColumn<PermissionDTO, String> statusCol;  // עמודת סטטוס בטבלת ההרשאות, כ-String


    @FXML
    private Button acceptButton;  // כפתור לאישור הרשאה

    @FXML
    private Button rejectButton;  // כפתור לדחיית הרשאה

    @FXML
    private Button requestReaderButton;

    @FXML
    private Button requestWriterButton;



    @FXML
    private Button openSheetViewfinder;  // כפתור לדחיית הרשאה

    @FXML
    private Label selectedUserName;  // תווית שם המשתמש הנבחר בטבלת ההרשאות





    @FXML
    public void initialize() {

        allButtonsDisbled();




        labelForStatus.setText("");
        usersListComponentController.setHttpStatusUpdate(this);
        accountCommands = this;
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        initTableColumns();  // אתחול העמודות בעת ההפעלה
        //startSheetListRefresher();   // התחלת עדכון הטבלה כל 2 שניות (REFRESH_RATE)
        sheetPremmisionTable.setVisible(false);


        chatAreaComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        chatAreaComponentController.startListRefresher();


        permissionUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionCol.setCellValueFactory(new PropertyValueFactory<>("permission"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));





        /*
// מאזין ללחיצה על שורה בטבלה - לחיצה אחת במקום לחיצה כפולה
        sheetTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) { // שינוי ללחיצה אחת
                SheetInfoDto selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
                if (selectedSheet != null) {
                    selectedSheetName = selectedSheet.getSheetName();  // שמירת שם הגיליון הנבחר
                    System.out.println("Selected sheet: " + selectedSheetName);  // הדפסת שם הגיליון שנבחר

                    // עדכון תווית שם הגיליון בכרטיסייה
                    selectedSheetNameLabel.setText(selectedSheetName);

                    // בקשת הרשאות למשתמש הנוכחי
                    String userAccess = selectedSheet.getAccess();

                    // עדכון מצב הכפתור של צפייה בגיליון
                    openSheetViewfinder.setDisable(!("owner".equals(userAccess) || "write".equals(userAccess) || "read".equals(userAccess)));

                    // קביעה מה להציג בהתאם לסוג ההרשאה
                    switch (userAccess) {
                        case "owner":
                            sheetPremmisionTable.setVisible(true);
                            statusCol.setVisible(true);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            requestWriterButton.setDisable(true);
                            requestReaderButton.setDisable(true);
                            loadPermissionsForOwner(selectedSheet.getSheetName());
                            readerUser = false;
                            openSheetViewfinder.setDisable(false);
                            labelForStatus.setVisible(false);
                            break;
                        case "write":
                            sheetPremmisionTable.setVisible(true);
                            statusCol.setVisible(false);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            requestWriterButton.setDisable(true);
                            requestReaderButton.setDisable(true);
                            loadPermissionsForEditorOrViewer(selectedSheet.getSheetName());
                            readerUser = false;
                            openSheetViewfinder.setDisable(false);
                            labelForStatus.setVisible(false);
                            break;
                        case "read":
                            sheetPremmisionTable.setVisible(true);
                            statusCol.setVisible(false);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            requestWriterButton.setDisable(false);
                            requestReaderButton.setDisable(true);
                            loadPermissionsForEditorOrViewer(selectedSheet.getSheetName());
                            readerUser = true;
                            openSheetViewfinder.setDisable(false);
                            labelForStatus.setVisible(false);
                            break;

                        case "no access":
                            sheetPremmisionTable.setVisible(false);
                            requestWriterButton.setDisable(false);
                            requestReaderButton.setDisable(false);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            userRequestStatus();
                            openSheetViewfinder.setDisable(true);
                            labelForStatus.setVisible(true);
                            break;
                    }

                    Platform.runLater(() -> sheetTableView.getSelectionModel().clearSelection());
                }

            }
        });

         */


        sheetTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                SheetInfoDto selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
                if (selectedSheet != null) {
                    selectedSheetName = selectedSheet.getSheetName();
                    selectedSheetNameLabel.setText(selectedSheetName);

                    String userAccess = selectedSheet.getAccess();

                    // עצירת רענון קודם
                    stopPermissionListRefresher();

                    switch (userAccess) {
                        case "owner":
                            sheetPremmisionTable.setVisible(true);
                            statusCol.setVisible(true);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            requestWriterButton.setDisable(true);
                            requestReaderButton.setDisable(true);
                            readerUser = false;
                            openSheetViewfinder.setDisable(false);
                            labelForStatus.setVisible(false);

                            // התחלת רענון הרשאות לבעלים
                            startPermissionListRefresher(selectedSheetName, true);
                            break;
                        case "write":
                        case "read":
                            sheetPremmisionTable.setVisible(true);
                            statusCol.setVisible(false);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            requestWriterButton.setDisable("read".equals(userAccess));
                            requestReaderButton.setDisable(true);
                            readerUser = "read".equals(userAccess);
                            openSheetViewfinder.setDisable(false);
                            labelForStatus.setVisible(false);

                            // התחלת רענון הרשאות לעורך/קורא
                            startPermissionListRefresher(selectedSheetName, false);
                            break;
                        default:
                            sheetPremmisionTable.setVisible(false);
                            requestWriterButton.setDisable(false);
                            requestReaderButton.setDisable(false);
                            acceptButton.setDisable(true);
                            rejectButton.setDisable(true);
                            userRequestStatus();
                            openSheetViewfinder.setDisable(true);
                            labelForStatus.setVisible(true);

                            // עצירת רענון הרשאות
                            stopPermissionListRefresher();
                            break;
                    }

                    Platform.runLater(() -> sheetTableView.getSelectionModel().clearSelection());
                }
            }
        });





// מאזין ללחיצה על שורה בטבלה - לחיצה אחת במקום לחיצה כפולה
        sheetPremmisionTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) { // שינוי ללחיצה אחת
                PermissionDTO selectedPermission = sheetPremmisionTable.getSelectionModel().getSelectedItem();
                if (selectedPermission != null) {
                    selectedUserName.setText(selectedPermission.getUsername());
                    selectedUsername = selectedPermission.getUsername();

                    // קביעת האם הכפתורים יושבתו בהתאם לסטטוס
                    boolean isPending = "Pending".equals(selectedPermission.getStatus());
                    acceptButton.setDisable(!isPending);
                    rejectButton.setDisable(!isPending);
                } else {
                    selectedUsername = null;
                    acceptButton.setDisable(true);
                    rejectButton.setDisable(true);
                }
            }
        });


        // אתחול הכפתור לפי ערך ברירת המחדל (אם לא ניתן appMainController עדיין)
        if (appMainController != null) {
            darkModeToggle.setSelected(appMainController.isDarkMode());
        }


    }

    private void allButtonsDisbled(){
        openSheetViewfinder.setDisable(true);
        acceptButton.setDisable(true);
        rejectButton.setDisable(true);
        requestReaderButton.setDisable(true);
        requestWriterButton.setDisable(true);
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




    public void userRequestStatus() {
        final Gson gson = new Gson();
        // יצירת בקשת GET עם OkHttp
        Request request = new Request.Builder()
                .url(REQUEST_URL)
                .get()
                .build();

        // קריאה אסינכרונית בעזרת HttpClientUtil והגדרת Callback
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> labelForStatus.setText("Error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseBodyString = responseBody != null ? responseBody.string() : "";

                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            if (responseBodyString.isEmpty()) {
                                labelForStatus.setText("No pending requests.");
                            } else {
                                // ניתוח ה-JSON לתצוגה ברורה
                                Type listType = new TypeToken<List<Map<String, String>>>(){}.getType();
                                List<Map<String, String>> requests = gson.fromJson(responseBodyString, listType);

                                StringBuilder displayText = new StringBuilder("Pending Requests:\n");
                                for (Map<String, String> request : requests) {
                                    displayText.append("").append(request.get("requestedPermission"))
                                            .append(" -  Status: ").append(request.get("status")).append("\n");
                                }
                                labelForStatus.setText(displayText.toString());
                            }
                        } else {
                            labelForStatus.setText("Server error: " + responseBodyString);
                        }
                    });
                }
            }
        });
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
            stopPermissionListRefresher(); // הפסקת רענון ההרשאות
        } catch (Exception ignored) {}
    }

    public void setChatAppMainController(AppMainController chatAppMainController) {
        this.chatAppMainController = chatAppMainController;
        // התאמת המצב של Toggle Button לפי מצב darkMode
        darkModeToggle.setSelected(chatAppMainController.isDarkMode());

        // מאזין לשינויים בכפתור
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            chatAppMainController.setDarkMode(newValue); // עדכון מצב darkMode ב-AppMainController
        });
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
                // הצגת הודעת שגיאה ל-7 שניות
                showTemporaryMessage(loadingStatusLabel, e.getMessage(), 7);
            }
        } else {
            // הצגת הודעה על חוסר קובץ ל-7 שניות
            showTemporaryMessage(loadingStatusLabel, "No file selected or an error occurred.", 7);
        }
    }

    // פונקציה שתציג הודעה לזמן מוגבל
    private void showTemporaryMessage(Label label, String message, int seconds) {
        label.setText(message);

        // יצירת Timeline שיאפס את ההודעה לאחר X שניות
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(seconds),
                event -> label.setText("")));
        timeline.setCycleCount(1); // פועל רק פעם אחת
        timeline.play();
    }





    @FXML
    // פעולה לפתיחת ה-Viewfinder
    public void openSheetViewfinder() {
        if (selectedSheetName != null) {
            chatAppMainController.switchToViewfinder(selectedSheetName, readerUser);  // מעבר ל-viewfinder
        } else {
            System.out.println("No sheet selected");
        }
    }


    /*

    private void uploadFileToServer(File file) throws IOException {

        String finalUrl = UPLOAD_SHEET_URL;  // משתמש באותו BASE_URL כמו שאר הקוד

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
                        loadingStatusLabel.setText("Error during file upload: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseBodyString = responseBody != null ? responseBody.string() : "No response body";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            //loadingStatusLabel.setText("Success: " + responseBodyString);
                        } else {
                            loadingStatusLabel.setText( responseBodyString );
                        }
                    });
                }
            }
        });
    }


     */


    private void uploadFileToServer(File file) throws IOException {

        String finalUrl = UPLOAD_SHEET_URL;  // משתמש באותו BASE_URL כמו שאר הקוד

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
                        showTemporaryMessage(loadingStatusLabel, e.getMessage(), 4)
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseBodyString = responseBody != null ? responseBody.string() : "No response body";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            // הצגת הודעת הצלחה למשך 7 שניות
                            //showTemporaryMessage(loadingStatusLabel, "Upload succeeded", 4);
                        } else {
                            // הצגת הודעת שגיאה מהשרת למשך 7 שניות
                            showTemporaryMessage(loadingStatusLabel, responseBodyString, 4);
                        }
                    });
                }
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













    // התחלת רענון הרשאות
    private void startPermissionListRefresher(String sheetName, boolean isOwner) {
        stopPermissionListRefresher(); // מפסיקים רענון קודם אם יש

        // בחירת פונקציית עדכון מתאימה לפי סוג המשתמש
        Consumer<List<PermissionDTO>> updatePermissionTable = isOwner
                ? this::updatePermissionsForOwner
                : this::updatePermissionsForEditorOrViewer;

        permissionListRefresher = new PermissionListRefresher(
                sheetName,
                this::logHttpRequest,
                updatePermissionTable
        );
        permissionTimer = new Timer();
        permissionTimer.schedule(permissionListRefresher, 0, Constants.REFRESH_RATE);
    }

    // הפסקת רענון הרשאות
    private void stopPermissionListRefresher() {
        if (permissionListRefresher != null && permissionTimer != null) {
            permissionListRefresher.cancel();
            permissionTimer.cancel();
            permissionTimer.purge();
            permissionListRefresher = null;
            permissionTimer = null;
        }
    }


/*
    // עדכון טבלת הרשאות לבעלים
    private void updatePermissionsForOwner(List<PermissionDTO> permissionList) {
        Platform.runLater(() -> {
            ObservableList<PermissionDTO> permissions = FXCollections.observableArrayList(permissionList);
            sheetPremmisionTable.setItems(permissions);
            statusCol.setVisible(true); // הצגת עמודת הסטטוס לבעלים
        });
    }

    // עדכון טבלת הרשאות לעורך או קורא
    private void updatePermissionsForEditorOrViewer(List<PermissionDTO> permissionList) {
        Platform.runLater(() -> {
            ObservableList<PermissionDTO> approvedPermissions = FXCollections.observableArrayList();
            for (PermissionDTO permission : permissionList) {
                if (!"pending".equalsIgnoreCase(permission.getStatus())) {
                    approvedPermissions.add(permission);
                }
            }
            sheetPremmisionTable.setItems(approvedPermissions);
            statusCol.setVisible(false); // הסתרת עמודת הסטטוס
        });
    }

 */

    // עדכון טבלת הרשאות לבעלים, רק אם הרשימה השתנתה
    private void updatePermissionsForOwner(List<PermissionDTO> permissionList) {
        if (!isPermissionsListEqual(currentPermissions, permissionList)) {
            currentPermissions = new ArrayList<>(permissionList); // עדכון הרשימה הקיימת

            Platform.runLater(() -> {
                ObservableList<PermissionDTO> permissions = FXCollections.observableArrayList(permissionList);
                sheetPremmisionTable.setItems(permissions);
                statusCol.setVisible(true); // הצגת עמודת הסטטוס לבעלים
            });
        }
    }

    // עדכון טבלת הרשאות לעורך או קורא, רק אם הרשימה השתנתה
    private void updatePermissionsForEditorOrViewer(List<PermissionDTO> permissionList) {
        // סינון בקשות ממתינות
        List<PermissionDTO> filteredPermissions = permissionList.stream()
                .filter(permission -> !"pending".equalsIgnoreCase(permission.getStatus()))
                .toList();

        if (!isPermissionsListEqual(currentPermissions, filteredPermissions)) {
            currentPermissions = new ArrayList<>(filteredPermissions); // עדכון הרשימה הקיימת

            Platform.runLater(() -> {
                ObservableList<PermissionDTO> approvedPermissions = FXCollections.observableArrayList(filteredPermissions);
                sheetPremmisionTable.setItems(approvedPermissions);
                statusCol.setVisible(false); // הסתרת עמודת הסטטוס
            });
        }
    }


    private boolean isPermissionsListEqual(List<PermissionDTO> list1, List<PermissionDTO> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            PermissionDTO p1 = list1.get(i);
            PermissionDTO p2 = list2.get(i);
            if (!p1.getUsername().equals(p2.getUsername()) ||
                    !p1.getPermission().equals(p2.getPermission()) ||
                    !p1.getStatus().equals(p2.getStatus())) {
                return false;
            }
        }
        return true;
    }



    public void setAppMainController(AppMainController appMainControll) {
        this.appMainController = appMainControll;

        // התאמת המצב של Toggle Button לפי מצב darkMode
        darkModeToggle.setSelected(appMainController.isDarkMode());

        // מאזין לשינויים בכפתור
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            appMainController.setDarkMode(newValue); // עדכון מצב darkMode ב-AppMainController
        });
    }



    public void applyTheme(boolean darkMode) {
        if (mainPane != null) {
            mainPane.getStylesheets().clear();
            if (darkMode) {
                mainPane.getStylesheets().add(getClass().getResource("/component/accountarea/account-area-darkmode.css").toExternalForm());
            } else {
                mainPane.getStylesheets().add(getClass().getResource("/component/accountarea/account-area.css").toExternalForm());
            }
        }
    }



    public void setDarkMode(boolean darkMode) {
        darkModeToggle.setSelected(darkMode);
    }
}




