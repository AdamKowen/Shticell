package component.login;

import component.main.AppMainController;
import javafx.scene.layout.GridPane;
import util.Constants;
import util.http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import org.jetbrains.annotations.NotNull;

import okhttp3.*;

import java.io.IOException;

public class LoginController {


    @FXML
    public GridPane mainPane;

    @FXML
    public TextField userNameTextField;

    @FXML
    public Label errorMessageLabel;

    private AppMainController chatAppMainController;

    private final StringProperty errorMessageProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
        HttpClientUtil.setCookieManagerLoggingFacility(line ->
                Platform.runLater(() ->
                        updateHttpStatusLine(line)));
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {

        String userName = userNameTextField.getText();
        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        //noinspection ConstantConditions
        String finalUrl = HttpUrl
                        .parse(Constants.LOGIN_PAGE)
                        .newBuilder()
                        .addQueryParameter("username", userName)
                        .build()
                        .toString();

        System.out.println("Requesting URL: " + finalUrl);

        updateHttpStatusLine("New request is launched for: " + finalUrl);


        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            errorMessageProperty.set("Something went wrong: " + responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                            chatAppMainController.updateUserName(userName);
                            chatAppMainController.switchToChatRoom();
                    });
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    private void updateHttpStatusLine(String data) {
        chatAppMainController.updateHttpLine(data);
    }

    public void setChatAppMainController(AppMainController chatAppMainController) {
        this.chatAppMainController = chatAppMainController;
    }





    public void applyTheme(boolean darkMode) {
        if (mainPane != null) {
            mainPane.getStylesheets().clear();
            if (darkMode) {
                mainPane.getStylesheets().add(getClass().getResource("/component/login/login-darkmode.css").toExternalForm());
            } else {
                mainPane.getStylesheets().add(getClass().getResource("/component/login/login.css").toExternalForm());
            }
        }
    }
}
