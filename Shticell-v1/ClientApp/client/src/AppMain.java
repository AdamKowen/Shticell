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







/*
import java.io.IOException;
import java.net.URL;

import component.main.AppMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.http.HttpClientUtil;

import static util.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class AppMain extends Application {

    private AppMainController chatAppMainController;
    // Variables to store the offset between mouse position and window position
    private double xOffset = 0;
    private double yOffset = 0;

    // Define a margin of 10 pixels from each edge where resizing is enabled
    private static final int RESIZE_MARGIN = 10;

    // Flags to track if we are currently in a resize mode and in which direction
    private boolean isResizeRight = false;
    private boolean isResizeBottom = false;
    private boolean isResizeLeft = false;
    private boolean isResizeTop = false;

    @Override
    public void start(Stage primaryStage) {
        // Remove standard window decorations (including the "traffic lights" buttons) by setting StageStyle.TRANSPARENT
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setTitle("Shticell");

        URL loginPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();
            chatAppMainController = fxmlLoader.getController();

            // If scaling needs to be applied – here we use 1 (no scaling)
            root.setScaleX(1);
            root.setScaleY(1);

            // Create a scene with transparent background – required for rounded corners effect
            Scene scene = new Scene(root, 800, 600);
            scene.setFill(Color.TRANSPARENT);

            // Create a clip with rounded corners for the entire window
            Rectangle clip = new Rectangle();
            clip.setArcWidth(40);  // The roundness value can be changed as needed
            clip.setArcHeight(40);
            // Bind the size of the clip to the scene size
            clip.widthProperty().bind(scene.widthProperty());
            clip.heightProperty().bind(scene.heightProperty());

            root.setClip(clip);

            // Add functionality to drag the window
            // Dragging will be enabled when clicking in the top region of the window (e.g., top 40 pixels)
            root.setOnMousePressed((MouseEvent event) -> {
                if (event.getSceneY() < 40) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                }
            });
            root.setOnMouseDragged((MouseEvent event) -> {
                if (event.getSceneY() < 40) {
                    primaryStage.setX(event.getScreenX() - xOffset);
                    primaryStage.setY(event.getScreenY() - yOffset);
                }
            });

            primaryStage.setScene(scene);
            primaryStage.show();

            // Enable window resizing functionality
            enableWindowResize(primaryStage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableWindowResize(Stage stage, Parent root) {
        // Track mouse movement on the scene
        root.setOnMouseMoved(event -> {
            // Get mouse position
            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getBoundsInLocal().getWidth();
            double height = root.getBoundsInLocal().getHeight();

            // Reset all resize flags
            isResizeRight = false;
            isResizeBottom = false;
            isResizeLeft = false;
            isResizeTop = false;

            // Check if the mouse is near one of the edges
            // Right
            if (Math.abs(mouseX - width) < RESIZE_MARGIN) {
                isResizeRight = true;
            }
            // Left
            else if (mouseX < RESIZE_MARGIN) {
                isResizeLeft = true;
            }

            // Top
            if (mouseY < RESIZE_MARGIN) {
                isResizeTop = true;
            }
            // Bottom
            else if (Math.abs(mouseY - height) < RESIZE_MARGIN) {
                isResizeBottom = true;
            }
        });

        // When dragging the mouse
        root.setOnMouseDragged(event -> {
            // Get current mouse position
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();

            // Update dimensions as needed
            if (isResizeRight) {
                stage.setWidth(mouseX - stage.getX());
            }
            if (isResizeBottom) {
                stage.setHeight(mouseY - stage.getY());
            }
            if (isResizeLeft) {
                double oldX = stage.getX();
                double newX = mouseX;
                double diffX = oldX - newX;
                stage.setX(newX);
                stage.setWidth(stage.getWidth() + diffX);
            }
            if (isResizeTop) {
                double oldY = stage.getY();
                double newY = mouseY;
                double diffY = oldY - newY;
                stage.setY(newY);
                stage.setHeight(stage.getHeight() + diffY);
            }
        });
    }

    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        if (chatAppMainController != null) {
            chatAppMainController.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}



 */


