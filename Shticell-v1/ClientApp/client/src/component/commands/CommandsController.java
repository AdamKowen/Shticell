package component.commands;

import component.api.ChatCommands;
import javafx.stage.FileChooser;
import util.Constants;
import util.http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
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
        String boundary = "===" + System.currentTimeMillis() + "===";
        String serverUrl = "http://localhost:8080/webEngine_Web_exploded/uploadSheet";

        HttpURLConnection connection = (HttpURLConnection) new URL(serverUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream()) {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            // כותב את הנתונים על הקובץ
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append("\r\n\r\n");
            writer.flush();

            // מעתיק את הקובץ לתוך זרם היציאה
            Files.copy(file.toPath(), outputStream);
            outputStream.flush();

            // מסיים את ה-body של ה-request
            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n");
            writer.close();

            // קריאת תגובת השרת
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // קבלת הודעת הצלחה מהשרת
                InputStream responseStream = connection.getInputStream();
                String responseMessage = new BufferedReader(new InputStreamReader(responseStream))
                        .lines().collect(Collectors.joining("\n"));

                System.out.println("Server response: " + responseMessage);
            } else {
                System.out.println("Failed to upload file. Response code: " + responseCode);
            }
        } finally {
            connection.disconnect();
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
