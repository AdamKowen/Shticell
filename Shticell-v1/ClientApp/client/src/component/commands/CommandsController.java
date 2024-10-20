package component.commands;

import component.api.ChatCommands;
import javafx.stage.FileChooser;
import okhttp3.*;
import util.Constants;
import util.http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class CommandsController {

    private ChatCommands chatCommands;
    private final BooleanProperty autoUpdates;
    @FXML private ToggleButton autoUpdatesButton;

    public CommandsController() {
        autoUpdates = new SimpleBooleanProperty();
    }

    @FXML
    public void initialize() {
        autoUpdates.bind(autoUpdatesButton.selectedProperty());
    }

    public ReadOnlyBooleanProperty autoUpdatesProperty() {
        return autoUpdates;
    }

    @FXML
    void logoutClicked(ActionEvent event) {
        chatCommands.updateHttpLine(Constants.LOGOUT);
        HttpClientUtil.runAsync(Constants.LOGOUT, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                chatCommands.updateHttpLine("Logout request ended with failure...:(");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful() || response.isRedirect()) {
                    HttpClientUtil.removeCookiesOf(Constants.BASE_DOMAIN);
                    chatCommands.logout();
                }
            }
        });
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







    @FXML
    void quitClicked(ActionEvent event) {
        Platform.exit();
    }

    public void setChatCommands(ChatCommands chatRoomMainController) {
        this.chatCommands = chatRoomMainController;
    }
}
