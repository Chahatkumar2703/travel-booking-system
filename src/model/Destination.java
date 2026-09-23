package model;

/**
 * Destination entity representing a travel destination.
 */
public class Destination extends BaseEntity {

    private String name;
    private String state;
    private String description;
    private String attractions;
    private String bestTime;

    public Destination() {
        super();
    }

    public Destination(int id, String name, String state, String description, String attractions, String bestTime) {
        super(id);
        this.name = name;
        this.state = state;
        this.description = description;
        this.attractions = attractions;
        this.bestTime = bestTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttractions() {
        return attractions;
    }

    public void setAttractions(String attractions) {
        this.attractions = attractions;
    }

    public String getBestTime() {
        return bestTime;
    }

    public void setBestTime(String bestTime) {
        this.bestTime = bestTime;
    }

    @Override
    public String toString() {
        return name + ", " + state;
    }
}
