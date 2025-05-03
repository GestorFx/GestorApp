import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "ERROR");
        System.setProperty("org.slf4j.simpleLogger.log.org.mongodb", "off");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/UsuariosView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Mi aplicación JavaFX");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void hideStage(Stage stage) {
        stage.hide();
    }
}