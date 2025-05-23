import java.io.IOException;
import java.net.URL;

import component.main.AppMainController;
import javafx.scene.shape.Rectangle;
import javafx.stage.StageStyle;
import util.http.HttpClientUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import static util.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class AppMain extends Application {

    private AppMainController chatAppMainController;



    @Override
    public void start(Stage primaryStage) {



        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setTitle("Shticell");

        URL loginPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();
            chatAppMainController = fxmlLoader.getController();


            root.setScaleX(1);
            root.setScaleY(1);
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        chatAppMainController.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}