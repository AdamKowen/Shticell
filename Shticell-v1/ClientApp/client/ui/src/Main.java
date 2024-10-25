import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // טוען את קובץ ה-FXML
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sheetViewfinder.fxml")));

            // יצירת סצנה מה-FXML
            Scene scene = new Scene(root);

            // הגדרת כותרת
            primaryStage.setTitle("Shticell");

            // הגדרת הסצנה
            primaryStage.setScene(scene);
//

            // טוען את קובץ ה-CSS
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            //scene.getStylesheets().add(getClass().getResource("/stylesDarkMode.css").toExternalForm());

            // הגדרת רוחב וגובה מינימלי לחלון
            primaryStage.setMinWidth(800); // גודל מינימלי לרוחב
            primaryStage.setMinHeight(600); // גודל מינימלי לגובה
//

            // הצגת החלון
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}





