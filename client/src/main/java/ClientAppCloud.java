import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ClientAppCloud extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("cloud.fxml"));
        primaryStage.setTitle("Cloud Storage");
        primaryStage.setScene(new Scene(parent));
        primaryStage.show();

//        Scene scenePop = new Scene(FXMLLoader.load(getClass().getResource("popup.fxml")));
//        Stage popupStage = new Stage();
//        popupStage.setScene(scenePop);
//        popupStage.show();
    }
}
