import java.util.ArrayList;

public class Metadata {
    private String dataVersion;
    private String matchId;
    private ArrayList<String> participants = new ArrayList<>();

    public Metadata() {
    }

    public Metadata(String dataVersion, String matchId, ArrayList<String> participants) {
        this.dataVersion = dataVersion;
        this.matchId = matchId;
        this.participants = participants;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public String getMatchId() {
        return matchId;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }
}