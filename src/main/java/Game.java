import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
    private Metadata metadata;
    private Info info;

    public Game() {
    }

    public Game(Metadata metadata, Info info) {
        this.metadata = metadata;
        this.info = info;
    }

    public Metadata getMetadata() {
        return metadata;
    }


    public Info getInfo() {
        return info;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
