package component.main;

import component.api.HttpStatusUpdate;
import component.chatroom.ChatRoomMainController;
import component.login.LoginController;
import component.sheetViewfinder.SheetViewfinderController;
import component.status.StatusController;
import component.accountarea.AccountController;
import component.status.StatusController;
import component.users.UsersListController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static util.Constants.*;

public class AppMainController implements Closeable, HttpStatusUpdate {

    @FXML private Parent httpStatusComponent;
    @FXML private StatusController httpStatusComponentController;

    private GridPane loginComponent;
    private LoginController logicController;

    @FXML private BorderPane mainBorderPane;

    //private Parent chatRoomComponent;
    //private ChatRoomMainController chatRoomComponentController;
    //@FXML private UsersListController usersListComponentController;


    private Parent accountAreaComponent;
    private AccountController accountAreaController;

    private Parent viewfinderComponent;
    private SheetViewfinderController viewfinderController;


    @FXML private AnchorPane mainPanel;

    private final StringProperty currentUserName;

    public AppMainController() {
        currentUserName = new SimpleStringProperty(JHON_DOE);
    }

    @FXML
    public void initialize() {

        // prepare components
        loadLoginPage();
        loadAccountPage();
        loadViewfinderPage();
        //loadChatRoomPage();
    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }
    
    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    @Override
    public void close() throws IOException {
        accountAreaController.close();
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            logicController = fxmlLoader.getController();
            logicController.setChatAppMainController(this);
            mainBorderPane.setCenter(loginComponent); // Place in center
            //setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void loadAccountPage() {
        URL loginPageUrl = getClass().getResource(ACCOUNT_AREA_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            accountAreaComponent = fxmlLoader.load();
            accountAreaController = fxmlLoader.getController();
            accountAreaController.setChatAppMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadViewfinderPage() {
        URL viewfinderPageUrl = getClass().getResource("/component/sheetViewfinder/sheetViewfinder.fxml");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(viewfinderPageUrl);
            viewfinderComponent = fxmlLoader.load();
            viewfinderController = fxmlLoader.getController();
            viewfinderController.setAppMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    private void loadChatRoomPage() {
        URL loginPageUrl = getClass().getResource(CHAT_ROOM_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            chatRoomComponent = fxmlLoader.load();
            chatRoomComponentController = fxmlLoader.getController();
            chatRoomComponentController.setChatAppMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     */



    @Override
    public void updateHttpLine(String line) {
        //httpStatusComponentController.addHttpStatusLine(line);
        System.out.println(line);
    }

    public void switchToChatRoom() {
        //setMainPanelTo(accountAreaComponent);
        mainBorderPane.setCenter(accountAreaComponent); // Switch to account page
        accountAreaController.setActive();
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set(JHON_DOE);
            accountAreaController.setInActive();
            //setMainPanelTo(loginComponent);
            mainBorderPane.setCenter(loginComponent); // Switch back to login page
        });
    }



    public void switchToViewfinder(String sheetName) {
        viewfinderController.setSheet(sheetName);  // מעביר את השם של הגיליון הנבחר
        mainBorderPane.setCenter(viewfinderComponent);  // החלפה של התצוגה הראשית
    }



    @FXML
    public void openSheetViewfinder(String selectedSheetName) {
        if (selectedSheetName != null) {
            switchToViewfinder(selectedSheetName);  // מעבר ל-viewfinder
        } else {
            System.out.println("No sheet selected");
        }
    }

    public void switchToAccountArea() {
        mainBorderPane.setCenter(accountAreaComponent);  // חזרה למסך ה-account
    }


}
