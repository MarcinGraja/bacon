package bacon;
//--add-exports=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED
//        --add-exports=javafx.base/com.sun.javafx.reflect=ALL-UNNAMED
//        --add-exports=javafx.base/com.sun.javafx.beans=ALL-UNNAMED
//        --add-exports=javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
//        --add-exports=javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
import bacon.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        MainController mainController = new MainController();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/mainView.fxml"));
        fxmlLoader.setController(mainController.getMainView());
        primaryStage.setTitle("Degrees of separation");
        primaryStage.setScene(new Scene(fxmlLoader.load(), 600, 400));
        primaryStage.show();
    }
    public static void main(String []args){
        launch(args);
    }
}
