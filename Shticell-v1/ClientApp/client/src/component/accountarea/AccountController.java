package component.accountarea;

import component.api.AccountCommands;
import component.api.HttpStatusUpdate;
import component.commands.CommandsController;
import component.main.AppMainController;
import component.users.UsersListController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

public class AccountController implements Closeable, HttpStatusUpdate, AccountCommands {

    public GridPane sheetCard;
    private AccountCommands accountCommands;
    @FXML private VBox usersListComponent;
    @FXML private UsersListController usersListComponentController;
    @FXML private VBox actionCommandsComponent;
    @FXML private CommandsController actionCommandsComponentController;

    private AppMainController chatAppMainController;

    @FXML
    public void initialize() {
        usersListComponentController.setHttpStatusUpdate(this);
        accountCommands = this;
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




    public void setAccountCommands(AccountCommands chatRoomMainController) {
        this.accountCommands = chatRoomMainController;
    }
}

