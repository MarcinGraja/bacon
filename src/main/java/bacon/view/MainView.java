package bacon.view;

import bacon.controller.MainController;
import bacon.controller.ParallelRequest;
import bacon.model.Actor;
import bacon.model.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
                StringBuilder tooltip = new StringBuilder("id: ").append(actor.getID()).append("\nplayed in:\n");
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
        sourceField.setEditable(false);
        targetField.setEditable(false);
        foundActors.setItems(mainController.getMainModel().getFoundActors());
        foundActors.setCellFactory(lv-> new CustomCell());
        output.setEditable(false);
        searchButton.setOnAction(e-> mainController.addActorsByName(searchBox.getText()));
        output.textProperty().bind(mainController.getTask().messageProperty());
    }
    @SuppressWarnings("Duplicates")
    @FXML
    void fastStart(){
        Actor source = new Actor("Cezary Pazura","nm0668640");
        sourceField.setText("current source:" + source.getName());
        mainController.setSource(source);

        Actor target = new Actor("Henryk Golebiewski","nm0326430");
        targetField.setText("current target:" + target.getName());
        mainController.setTarget(target);
        start();
    }
    @FXML
    void start(){
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

