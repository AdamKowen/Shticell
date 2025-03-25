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

    // flag to indicate if user is a reader
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

    @FXML private ToggleButton darkModeToggle; // button to toggle dark mode
    @FXML BorderPane mainPane;

    private AppMainController chatAppMainController;
    private Timer timer;  // timer for sheet list refresher
    private TimerTask sheetListRefresher;  // task for refreshing the sheet list
    private Timer permissionTimer;
    private TimerTask permissionListRefresher;

    @FXML private TableView<SheetInfoDto> sheetTableView;
    @FXML private TableColumn<SheetInfoDto, String> nameColumn;  // column for sheet name
    @FXML private TableColumn<SheetInfoDto, Integer> rowsColumn;  // column for row count
    @FXML private TableColumn<SheetInfoDto, Integer> columnsColumn;  // column for column count
    @FXML private TableColumn<SheetInfoDto, String> ownerColumn;  // column for owner name
    @FXML private TableColumn<SheetInfoDto, String> accessColumn;  // column for access permission
    @FXML private Label labelForStatus;
    @FXML private Label selectedSheetNameLabel;  // label for selected sheet name
    @FXML private Label loadingStatusLabel;

    private String selectedSheetName = null;  // variable to store the selected sheet name
    private String selectedUsername = null;  // variable to store the selected username

    private AppMainController appMainController;

    @FXML private TableView<PermissionDTO> sheetPremmisionTable;
    @FXML private TableColumn<PermissionDTO, String> permissionUserCol;  // column for permission username
    @FXML private TableColumn<PermissionDTO, String> permissionCol;  // column for permission type
    @FXML private TableColumn<PermissionDTO, String> statusCol;  // column for status

    @FXML private Button acceptButton;  // button to accept permission
    @FXML private Button rejectButton;  // button to reject permission
    @FXML private Button requestReaderButton;
    @FXML private Button requestWriterButton;
    @FXML private Button openSheetViewfinder;  // button to open the viewfinder
    @FXML private Label selectedUserName;  // label for selected username in the permission table

    @FXML
    public void initialize() {
        allButtonsDisbled();

        labelForStatus.setText("");
        usersListComponentController.setHttpStatusUpdate(this);
        accountCommands = this;
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        initTableColumns();  // init columns at startup
        // startSheetListRefresher();   // start table refresh every 2 seconds (REFRESH_RATE)
        sheetPremmisionTable.setVisible(false);

        chatAreaComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        chatAreaComponentController.startListRefresher();

        permissionUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        permissionCol.setCellValueFactory(new PropertyValueFactory<>("permission"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        sheetTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) {
                SheetInfoDto selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
                if (selectedSheet != null) {
                    selectedSheetName = selectedSheet.getSheetName();
                    selectedSheetNameLabel.setText(selectedSheetName);

                    String userAccess = selectedSheet.getAccess();

                    // stop previous refresher
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

                            // start permission refresher for owner
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

                            // start permission refresher for editor/viewer
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

                            // stop permission refresher
                            stopPermissionListRefresher();
                            break;
                    }
                    Platform.runLater(() -> sheetTableView.getSelectionModel().clearSelection());
                }
            }
        });

        // listener for single click on permission table row instead of double click
        sheetPremmisionTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 1) { // change to single click
                PermissionDTO selectedPermission = sheetPremmisionTable.getSelectionModel().getSelectedItem();
                if (selectedPermission != null) {
                    selectedUserName.setText(selectedPermission.getUsername());
                    selectedUsername = selectedPermission.getUsername();

                    // set buttons enabled based on status
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

        // init dark mode toggle if appMainController is already set
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
                    Platform.runLater(() -> updateHttpLine("error sending " + permissionType + " request: " + e.getMessage()));
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
        // creating a get request with okhttp
        Request request = new Request.Builder()
                .url(REQUEST_URL)
                .get()
                .build();

        // async call using HttpClientUtil with a callback
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> labelForStatus.setText("error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    String responseBodyString = responseBody != null ? responseBody.string() : "";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            if (responseBodyString.isEmpty()) {
                                labelForStatus.setText("no pending requests.");
                            } else {
                                // parse json for a clear display
                                Type listType = new TypeToken<List<Map<String, String>>>(){}.getType();
                                List<Map<String, String>> requests = gson.fromJson(responseBodyString, listType);

                                StringBuilder displayText = new StringBuilder("pending requests:\n");
                                for (Map<String, String> request : requests) {
                                    displayText.append("").append(request.get("requestedPermission"))
                                            .append(" -  status: ").append(request.get("status")).append("\n");
                                }
                                labelForStatus.setText(displayText.toString());
                            }
                        } else {
                            labelForStatus.setText("server error: " + responseBodyString);
                        }
                    });
                }
            }
        });
    }

    // call for different types of permission requests:
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
            String url = Constants.APPROVAL_REQUEST_URL; // url to the new servlet
            System.out.println("url to servlet: " + url);
            System.out.println("selected sheet name: " + selectedSheetName);
            System.out.println("selected username: " + selectedUsername);

            RequestBody body = new FormBody.Builder()
                    .add("username", selectedUsername)
                    .add("sheetName", selectedSheetName)
                    .add("status", "APPROVED")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body) // using post for a simple request
                    .build();

            System.out.println("sending http request...");

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        String errorMessage = "error accepting request: " + e.getMessage();
                        System.out.println(errorMessage);
                        updateHttpLine(errorMessage);
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "no response body";
                    Platform.runLater(() -> {
                        System.out.println("response code: " + response.code());
                        System.out.println("response message: " + response.message());
                        System.out.println("response body: " + responseBody);
                        updateHttpLine("request accepted: " + response.message());
                    });
                    response.close();
                }
            });
        } else {
            System.out.println("either selectedSheetName or selectedUsername is null");
        }
    }

    @FXML
    private void rejectRequest() {
        if (selectedSheetName != null && selectedUsername != null) {
            String url = Constants.APPROVAL_REQUEST_URL; // url to the new servlet
            System.out.println("url to servlet: " + url);
            System.out.println("selected sheet name: " + selectedSheetName);
            System.out.println("selected username: " + selectedUsername);

            RequestBody body = new FormBody.Builder()
                    .add("username", selectedUsername)
                    .add("sheetName", selectedSheetName)
                    .add("status", "DENIED")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body) // using post for a simple request
                    .build();

            System.out.println("sending http request...");

            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        String errorMessage = "error rejecting request: " + e.getMessage();
                        System.out.println(errorMessage);
                        updateHttpLine(errorMessage);
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "no response body";
                    Platform.runLater(() -> {
                        System.out.println("response code: " + response.code());
                        System.out.println("response message: " + response.message());
                        System.out.println("response body: " + responseBody);
                        updateHttpLine("request rejected: " + response.message());
                    });
                    response.close();
                }
            });
        } else {
            System.out.println("either selectedSheetName or selectedUsername is null");
        }
    }

    @FXML
    void logoutClicked(ActionEvent event) {
        accountCommands.updateHttpLine(Constants.LOGOUT);
        HttpClientUtil.runAsync(Constants.LOGOUT, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                accountCommands.updateHttpLine("logout request ended with failure...:(");
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
        startSheetListRefresher();  // start updating the table here
    }

    public void setInActive() {
        try {
            usersListComponentController.close();
            stopSheetListRefresher();  // stop auto updates
            stopPermissionListRefresher(); // stop permission refresher
        } catch (Exception ignored) {}
    }

    public void setChatAppMainController(AppMainController chatAppMainController) {
        this.chatAppMainController = chatAppMainController;
        // set toggle button state based on dark mode
        darkModeToggle.setSelected(chatAppMainController.isDarkMode());

        // listener for toggle changes
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            chatAppMainController.setDarkMode(newValue); // update dark mode in app main controller
        });
    }

    @Override
    public void logout() {
        chatAppMainController.switchToLogin();
    }

    @FXML
    void loadSheetClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("select file");

        // adding a filter to allow only xml files (optional)
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // send file to server
                uploadFileToServer(file);
            } catch (IOException e) {
                e.printStackTrace();
                // show error message for 7 seconds
                showTemporaryMessage(loadingStatusLabel, e.getMessage(), 7);
            }
        } else {
            // show message for no file selected for 7 seconds
            showTemporaryMessage(loadingStatusLabel, "no file selected or an error occurred.", 7);
        }
    }

    // function to display a message for a limited time
    private void showTemporaryMessage(Label label, String message, int seconds) {
        label.setText(message);
        // create timeline to reset the message after x seconds
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(seconds),
                event -> label.setText("")));
        timeline.setCycleCount(1); // runs only once
        timeline.play();
    }

    @FXML
    // action to open the viewfinder
    public void openSheetViewfinder() {
        if (selectedSheetName != null) {
            chatAppMainController.switchToViewfinder(selectedSheetName, readerUser);  // switch to viewfinder
        } else {
            System.out.println("no sheet selected");
        }
    }

    private void uploadFileToServer(File file) throws IOException {
        String finalUrl = UPLOAD_SHEET_URL;  // using same base url as the rest of the code

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("application/xml")))
                .build();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)  // using post instead of get
                .build();

        // using the same http client with the same cookie manager
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
                    String responseBodyString = responseBody != null ? responseBody.string() : "no response body";
                    Platform.runLater(() -> {
                        if (response.isSuccessful()) {
                            // show success message for 7 seconds
                            //showTemporaryMessage(loadingStatusLabel, "upload succeeded", 4);
                        } else {
                            // show server error message for 7 seconds
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
        accessColumn.setCellValueFactory(new PropertyValueFactory<>("access"));  // link access column to field access
    }

    // function to start the periodic refresher for the sheet list
    private void startSheetListRefresher() {
        sheetListRefresher = new component.accountarea.SheetListRefresher(
                this::logHttpRequest,
                this::updateSheetTable);
        timer = new Timer();
        timer.schedule(sheetListRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    // update the table with new data
    private void updateSheetTable(List<SheetInfoDto> sheetInfoDtoList) {
        Platform.runLater(() -> {
            ObservableList<SheetInfoDto> data = FXCollections.observableArrayList(sheetInfoDtoList);
            sheetTableView.setItems(data);
        });
    }

    // log the http request (for debugging/tracking)
    private void logHttpRequest(String logMessage) {
        System.out.println(logMessage);  // can be used to track requests
    }

    private void displaySheetDetails(SheetInfoDto selectedSheet) {
        // show the selected sheet name
        selectedSheetNameLabel.setText(selectedSheet.getSheetName());

        // check if user is the owner of the sheet
        if (selectedSheet.getAccess().equals("owner")) {
            // if owner, load all permissions including pending
            loadPermissionsForOwner(selectedSheet.getSheetName());
        } else {
            // if user has write or read access
            if ("write".equals(selectedSheet.getAccess()) || "read".equals(selectedSheet.getAccess())) {
                loadPermissionsForEditorOrViewer(selectedSheet.getSheetName());
            } else {
                // if no permission
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
        // url of the servlet with sheet name as parameter
        String url = Constants.SHEET_PERMISSION_URL + sheetName;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // perform async request
        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> updateHttpLine("error fetching permissions for owner: " + e.getMessage()));
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
                        statusCol.setVisible(true); // show status column for owner
                    });
                } else {
                    Platform.runLater(() -> updateHttpLine("error fetching permissions for owner: " + response.code()));
                }
                response.close();  // close response to avoid leaks
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
                Platform.runLater(() -> updateHttpLine("error fetching permissions for viewer/editor: " + e.getMessage()));
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
                        statusCol.setVisible(false); // hide status column
                    });
                } else {
                    Platform.runLater(() -> updateHttpLine("error fetching permissions for viewer/editor: " + response.code()));
                }
                response.close();  // close response to avoid leaks
            }
        });
    }

    // start permission refresher
    private void startPermissionListRefresher(String sheetName, boolean isOwner) {
        stopPermissionListRefresher(); // stop previous refresher if any

        // choose update function based on user type
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

    // stop permission refresher
    private void stopPermissionListRefresher() {
        if (permissionListRefresher != null && permissionTimer != null) {
            permissionListRefresher.cancel();
            permissionTimer.cancel();
            permissionTimer.purge();
            permissionListRefresher = null;
            permissionTimer = null;
        }
    }

    // update permission table for owner, only if list has changed
    private void updatePermissionsForOwner(List<PermissionDTO> permissionList) {
        if (!isPermissionsListEqual(currentPermissions, permissionList)) {
            currentPermissions = new ArrayList<>(permissionList); // update current list

            Platform.runLater(() -> {
                ObservableList<PermissionDTO> permissions = FXCollections.observableArrayList(permissionList);
                sheetPremmisionTable.setItems(permissions);
                statusCol.setVisible(true); // show status column for owner
            });
        }
    }

    // update permission table for editor/viewer, only if list has changed
    private void updatePermissionsForEditorOrViewer(List<PermissionDTO> permissionList) {
        // filter out pending requests
        List<PermissionDTO> filteredPermissions = permissionList.stream()
                .filter(permission -> !"pending".equalsIgnoreCase(permission.getStatus()))
                .toList();

        if (!isPermissionsListEqual(currentPermissions, filteredPermissions)) {
            currentPermissions = new ArrayList<>(filteredPermissions); // update current list

            Platform.runLater(() -> {
                ObservableList<PermissionDTO> approvedPermissions = FXCollections.observableArrayList(filteredPermissions);
                sheetPremmisionTable.setItems(approvedPermissions);
                statusCol.setVisible(false); // hide status column
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

        // set dark mode toggle state based on app main controller
        darkModeToggle.setSelected(appMainController.isDarkMode());

        // listener for toggle changes
        darkModeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            appMainController.setDarkMode(newValue); // update dark mode in app main controller
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
