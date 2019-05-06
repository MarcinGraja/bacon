package bacon.model;

public class Movie extends org.jgrapht.graph.DefaultEdge{
    private String title;
    private String id;
    public Movie(String title, String id) {
        this.title = title;
        this.id = id;
    }
    public Movie(){}
    public String getTitle() {
        return title;
    }

    public String getID() {
        return id;
    }
}
