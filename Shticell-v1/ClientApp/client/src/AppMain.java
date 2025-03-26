



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
    // משתנים לאחסון ההפרש בין מיקום העכבר למיקום החלון
    private double xOffset = 0;
    private double yOffset = 0;


    // נגדיר מרווח של 10 פיקסלים מכל צד שבו ניתן לבצע שינוי גודל
    private static final int RESIZE_MARGIN = 10;

    // נשמור מידע על האם אנחנו כרגע ב"מצב גרירה" לשינוי גודל, ואיפה
    private boolean isResizeRight = false;
    private boolean isResizeBottom = false;
    private boolean isResizeLeft = false;
    private boolean isResizeTop = false;


    @Override
    public void start(Stage primaryStage) {
        // הסרת העיטוף הסטנדרטי (כולל לחצני הרמזור) על ידי הגדרת StageStyle.TRANSPARENT
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

            // אם יש צורך לשנות סקאלה – כאן משתמשים בערכים 1 (ללא שינוי)
            root.setScaleX(1);
            root.setScaleY(1);

            // יצירת סצנה עם רקע שקוף – חיוני להציג את אפקט הפינות המעוגלות
            Scene scene = new Scene(root, 800, 600);
            scene.setFill(Color.TRANSPARENT);

            // יצירת clip עם פינות מעוגלות עבור כל החלון
            Rectangle clip = new Rectangle();
            clip.setArcWidth(40);  // ערך המעגלת ניתן לשינוי בהתאם לצורך
            clip.setArcHeight(40);
            // קושר את הגודל של ה-clip לגודל הסצנה
            clip.widthProperty().bind(scene.widthProperty());
            clip.heightProperty().bind(scene.heightProperty());

            root.setClip(clip);

            // הוספת פונקציונליות לגרירת החלון
            // הגרירה תתבצע כאשר נלחץ באזור העליון של החלון (למשל, בגובה 40 פיקסלים)
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


            // הפעלת פונקציונליות שינוי גודל
            enableWindowResize(primaryStage, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void enableWindowResize(Stage stage, Parent root) {
        // נעקוב אחר תנועות העכבר על הסצנה
        root.setOnMouseMoved(event -> {
            // נבדוק את מיקום העכבר
            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getBoundsInLocal().getWidth();
            double height = root.getBoundsInLocal().getHeight();

            // נאתחל
            isResizeRight = false;
            isResizeBottom = false;
            isResizeLeft = false;
            isResizeTop = false;

            // נבדוק אם העכבר קרוב לאחד הצדדים
            // ימינה
            if (Math.abs(mouseX - width) < RESIZE_MARGIN) {
                isResizeRight = true;
            }
            // שמאלה
            else if (mouseX < RESIZE_MARGIN) {
                isResizeLeft = true;
            }

            // למעלה
            if (mouseY < RESIZE_MARGIN) {
                isResizeTop = true;
            }
            // למטה
            else if (Math.abs(mouseY - height) < RESIZE_MARGIN) {
                isResizeBottom = true;
            }
        });

        // בעת גרירת העכבר
        root.setOnMouseDragged(event -> {
            // ניקח את המידות הנוכחיות
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();

            // נעדכן לפי הצורך
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


