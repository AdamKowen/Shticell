package component.commands;

import component.api.AccountCommands;
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

public class CommandsController {

    private AccountCommands accountCommands;
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


    @FXML
    void loadSheetClicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");

        // Adding a filter to allow only XML files
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // שליחת הקובץ לשרת
                uploadFileToServer(file);
            } catch (IOException e) {
                e.printStackTrace();
                // error for user
                System.out.println("Error occurred during file upload.");
            }
        }else {
            // Display an error message if no file is selected
            //fileNameLabel.setText("No file selected or an error occurred.");
        }
    }

    private void uploadFileToServer(File file) throws IOException {
        String RESOURCE = "/uploadSheet";
        String BASE_URL = "http://localhost:8080/webEngine_Web_exploded";

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(file, MediaType.parse("text/plain")))
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + RESOURCE)
                .post(body)
                .build();

        // Use HttpClientUtil instead of creating a new client
        HttpClientUtil.runAsync(String.valueOf(request), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        accountCommands.updateHttpLine("Error during file upload: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() ->
                            accountCommands.updateHttpLine("File uploaded successfully")
                    );
                } else {
                    String error = response.body().string();
                    Platform.runLater(() ->
                            accountCommands.updateHttpLine("Upload failed: " + error)
                    );
                }
            }
        });
    }




    @FXML
    void quitClicked(ActionEvent event) {
        Platform.exit();
    }

    public void setAccountCommands(AccountCommands chatRoomMainController) {
        this.accountCommands = chatRoomMainController;
    }
}
