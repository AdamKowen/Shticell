package component.accountarea;

import com.google.gson.Gson;
import component.api.AccountCommands;
import component.api.HttpStatusUpdate;
import component.chatarea.ChatAreaController;
import component.commands.CommandsController;
import component.main.AppMainController;
import component.sheetViewfinder.SheetViewfinderController;
import component.users.UsersListController;
import dto.SheetInfoDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private String selectedSheetName = null;  // משתנה לשמירת שם הגיליון הנבחר


    @FXML
    public void initialize() {
        usersListComponentController.setHttpStatusUpdate(this);
        accountCommands = this;
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        initTableColumns();  // אתחול העמודות בעת ההפעלה
        startSheetListRefresher();   // התחלת עדכון הטבלה כל 2 שניות (REFRESH_RATE)
        // מאזין ללחיצה על שורה בטבלה
        sheetTableView.setOnMouseClicked((MouseEvent event) -> {
            SheetInfoDto selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
            if (selectedSheet != null) {
                selectedSheetName = selectedSheet.getSheetName();  // שמירת שם הגיליון הנבחר
                System.out.println("Selected sheet: " + selectedSheetName);  // להדגמה
            }
        });


        chatAreaComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
        usersListComponentController.autoUpdatesProperty().bind(actionCommandsComponentController.autoUpdatesProperty());
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
    }

    public void setInActive() {
        try {
            usersListComponentController.close();
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
    }


    // הפונקציה שמתחילה את הרענון המחזורי של רשימת הגיליונות
    private void startSheetListRefresher() {
        sheetListRefresher = new component.sheets.SheetListRefresher(
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


}

