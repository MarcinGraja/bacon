package bacon.model;

public class Movie{
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
