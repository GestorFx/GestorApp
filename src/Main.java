import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb", "off");

    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void hideStage(Stage stage) {
        stage.hide();
    }
}