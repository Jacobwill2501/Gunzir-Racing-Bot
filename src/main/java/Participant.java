import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Participant {
    private String summonerId;
    private String summonerName;
    private boolean win;

    public Participant() {
    }

    public Participant(String summonerId, String summonerName, boolean win) {
        this.summonerId = summonerId;
        this.summonerName = summonerName;
        this.win = win;
    }

    public String getSummonerId() {
        return summonerId;
    }

    public String getSummonerName() {
        return summonerName;
    }

    public boolean isWin() {
        return win;
    }

    public void setSummonerId(String summonerId) {
        this.summonerId = summonerId;
    }

    public void setSummonerName(String summonerName) {
        this.summonerName = summonerName;
    }

    public void setWin(boolean win) {
        this.win = win;
    }
}
