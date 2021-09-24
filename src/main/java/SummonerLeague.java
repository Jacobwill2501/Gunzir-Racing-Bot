import net.rithms.riot.api.endpoints.league.dto.MiniSeries;

public class SummonerLeague {

    private String name;
    private String division;
    private String tier;
    private int LP;
    private int realDivision;
    private int realTier;
    private String series;
    private int wins = -1;
    private int streak;
    private String nickName;


    public SummonerLeague(String name, String division, int LP, String tier, int streak) {
        this.name = name;
        if(name.startsWith("Spirit")){
            this.nickName = "Carson";
        } else if(name.startsWith("gorilla")){
            this.nickName = "Gorilla";
        }else if(name.startsWith("tora")){
            this.nickName = "Gianni";
        } else{
            this.nickName = name;
        }
        this.tier = tier.toLowerCase();
        this.tier = this.tier.substring(0, 1).toUpperCase();
        if(this.tier == "G"){
            this.tier = "GM";
        }
        this.division = division;
        this.LP = LP;
        this.streak = streak;
        switch (division){
            case "I":
                realDivision = 1;
                break;
            case "II":
                realDivision = 2;
                break;
            case "III":
                realDivision = 3;
                break;
            case "IV":
                realDivision = 4;
                break;
            default:
                break;
        }

        switch (tier){
            case "CHALLENGER":
                realTier = 1;
                break;
            case "GRANDMASTER":
                realTier = 2;
                break;
            case "MASTER":
                realTier = 3;
                break;
            case "DIAMOND":
                realTier = 4;
                break;
            case "PLATINUM":
                realTier = 5;
                break;
            case "GOLD":
                realTier = 6;
                break;
            case "SILVER":
                realTier = 7;
                break;
            case "BRONZE":
                realTier = 8;
                break;
            case "IRON":
                realTier = 9;
                break;
            default:
                break;
        }
    }
    public SummonerLeague(String name, String division, int LP, String tier, int streak, MiniSeries series) {
        this.name = name;
        if(name.startsWith("Spirit")){
            this.nickName = "Carson";
        } else if(name.startsWith("gorilla")){
            this.nickName = "Gorilla";
        }else if(name.startsWith("tora")){
            this.nickName = "Gianni";
        } else{
            this.nickName = name;
        }
        this.wins = series.getWins();
        this.series = series.toString();
        this.series = this.series.replace('N','~');
        this.series = this.series.replace("", " ").trim();
        this.tier = tier.toLowerCase();
        this.tier = this.tier.substring(0, 1).toUpperCase();
        this.division = division;
        this.LP = LP;
        this.streak = streak;
        switch (division){
            case "I":
                realDivision = 1;
                break;
            case "II":
                realDivision = 2;
                break;
            case "III":
                realDivision = 3;
                break;
            case "IV":
                realDivision = 4;
                break;
            default:
                break;
        }

        switch (tier){
            case "CHALLENGER":
                realTier = 1;
                break;
            case "GRANDMASTER":
                realTier = 2;
                break;
            case "MASTER":
                realTier = 3;
                break;
            case "DIAMOND":
                realTier = 4;
                break;
            case "PLATINUM":
                realTier = 5;
                break;
            case "GOLD":
                realTier = 6;
                break;
            case "SILVER":
                realTier = 7;
                break;
            case "BRONZE":
                realTier = 8;
                break;
            case "IRON":
                realTier = 9;
                break;
            default:
                break;
        }
    }

    public String getSeries() {
        return series;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public int getLP() {
        return LP;
    }

    public void setLP(int LP) {
        this.LP = LP;
    }

    public int getRealDivision() {
        return realDivision;
    }

    public int getRealTier() {
        return realTier;
    }

    public String getTier() {
        return tier;
    }

    public int getWins() {
        return wins;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    @Override
    public String toString() {
        if(realTier == 1|| realTier == 2||realTier == 3){
            return "**" + nickName + "**: "+ tier + " - " + LP + "LP";
        } else if(LP == 100){
            return "**" + nickName + "**: "+ tier + realDivision + " - " + LP + "LP - " + series;
        }else{
            return "**" + nickName + "**: "+ tier + realDivision + " - " + LP + "LP";
        }
    }
}
