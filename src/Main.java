import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application
{
    private Parent root;
    private Stage stage;
    private Scene scene;


    public Stage getStage() {
        return stage;
    }

    public Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setMinHeight(800);
        stage.setMinWidth(1280);
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        initiate(stage);
    }

    private void setupGui() {

    }

    private void initiate(Stage stage) {
        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("GUI - Copy.fxml"));
            stage.setTitle("Hello World");
            stage.setScene(createScene());
            stage.show();
            MainController mainController = new MainController(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Class = Main, Method = initiate");
        }

    }

    private Scene createScene() {
        scene = new Scene(root, 1280, 800);
        return scene;
    }
}
