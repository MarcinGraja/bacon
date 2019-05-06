package bacon.view;

import bacon.controller.MainController;
import bacon.controller.ParallelRequest;
import bacon.model.Actor;
import bacon.model.Movie;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class MainView {
    private MainController mainController;
    @FXML
    private TextArea output;
    @FXML
    private TextField searchBox;
    @FXML
    private ListView<Actor> foundActors;
    @FXML
    private Button searchButton;
    @FXML
    private TextField sourceField;
    @FXML
    private TextField targetField;
    private class CustomCell extends ListCell<Actor>{
        @SuppressWarnings("Duplicates")
        public void updateItem(Actor actor, boolean b) {
            super.updateItem(actor, b);
            if (actor != null) {
                String url = mainController.getRequestURL(MainController.RequestType.MoviesByActor, actor.getID());
                ParallelRequest parallelRequest = new ParallelRequest(url);
                Thread thread = new Thread(parallelRequest);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Movie[] movies = mainController.parseMovies(parallelRequest.getResponse());
                StringBuilder tooltip = new StringBuilder("played in:\n");
                for (Movie movie : movies) {
                    tooltip.append(movie.getTitle()).append("\n");
                }
                setTooltip(new Tooltip(tooltip.toString()));
                setText(actor.getName());
            }
            else{
                setText(null);
                setGraphic(null);
            }
        }

    }
    @FXML private void initialize(){
        foundActors.setItems(mainController.getMainModel().getFoundActors());
        foundActors.setCellFactory(lv-> new CustomCell());
        output.setEditable(false);
        searchButton.setOnAction(e-> mainController.addActorsByName(searchBox.getText()));
        output.textProperty().bind(mainController.getTask().messageProperty());
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000),
                e-> {
//            System.out.println("fuck you");
//            System.out.println("checked message:"+mainController.getTask().getCheckedMessage());
                }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    @SuppressWarnings("Duplicates")
    @FXML
    void fuck(){
        System.out.println("adding some bacon");

        mainController.setSource(new Actor("Kevin Bacon","nm0000102"));
        System.out.println("adding some claws");
        mainController.setTarget(new Actor("Cezary Pazura","nm0668640"));
        start();
    }
    @FXML
    void start(){
        System.out.println("running shit");
        mainController.start();
    }
    @FXML
    void addSource(){
        mainController.setSource(foundActors.getSelectionModel().getSelectedItem());
        sourceField.setText("current source:" + foundActors.getSelectionModel().getSelectedItem().getName());
    }
    @FXML
    void addTarget(){
        mainController.setTarget(foundActors.getSelectionModel().getSelectedItem());
        targetField.setText("current target:" + foundActors.getSelectionModel().getSelectedItem().getName());
    }
    public MainView(MainController mainController) {
        this.mainController = mainController;
    }
}

