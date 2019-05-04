package bacon.model;

import java.util.Objects;

public class Actor {
    private String id;
    private String name;

    public Actor(String name, String id) {
        this.name = name;
        this.id = id;
    }
    public Actor(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actor actor = (Actor) o;
        return id.equals(actor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    public String toString(){
        return name;
    }
}
