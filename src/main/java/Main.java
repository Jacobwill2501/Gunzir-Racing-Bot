import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.league.dto.LeagueEntry;
import net.rithms.riot.api.endpoints.summoner.dto.Summoner;
import net.rithms.riot.constant.Platform;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;


import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;

/*
Gunzir Racing Bot isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone
officially involved in producing or managing Riot Games properties. Riot Games, and all associated properties are
trademarks or registered trademarks of Riot Games, Inc.
 */


public class Main extends ListenerAdapter {
    private static RiotApi api;
    private static double lastTime = 0;
    private static double lastTimeTFT = 0;
    private static final String RIOT_API_STRING = "https://americas.api.riotgames.com";
    private static final String RIOT_TFT_API = "https://na1.api.riotgames.com";
    private static String riotApiKey;

    public static void main(String[] args) throws Exception {
        riotApiKey = readFileAsString("src/main/java/riotkey.txt");
        ApiConfig config = new ApiConfig().setKey(riotApiKey);
        api = new RiotApi(config);
        String token = readFileAsString("src/main/java/discordkey.txt");
        JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Main())
                .setActivity(Activity.watching("Type $help for commands"))
                .build();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        String message = msg.getContentRaw();
        Guild guild = msg.getGuild();
        Role role = guild.getRoleById(887025249516920904L);

        /*
            $race - shows the current race of users from race.txt
                Players included will be names listed in race.txt
                Will show current hottest hot streak
                Will show current coldest cold streak
                Will sort the players by rank and display Name, Tier, Division, LP
                If in series, current series W/L
                ? Maybe add when someone gets into game ?

            $race add “name” - add a summoner name to race.txt
                Will display “Name was added to race!”
                If not valid summoner name, will remove name from race.txt

            $race remove “name” - removes a summoner name from race.txt
                Will display “Name was removed from race!”
                If not valid summoner name, will display and not remove any name from race.txt

=========================================================================================================================

            $racetft - shows the current tft race of users from racetft.txt
                Players included will be names listed in racetft.txt
                Will display name of who’s in the lead
                Will sort the players by rank and display Name, Tier, Division, LP

            $racetft add “name” - add a summoner name to racetft.txt
                Will display “Name was added to race!”
                If not valid summoner name, will remove name from racetft.txt

            $racetft remove “name” - removes a summoner name from racetft.txt
                Will display “Name was removed from race!”
                If not valid summoner name, will display and not remove any name from racetft.txt

         */

        /*
           $race - shows the current race of users from race.txt
         */
        if (message.equalsIgnoreCase("$race") && !(msg.getAuthor().isBot()) && inputDelay()) {
            //Get channel to send final String
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();

            //Display on console for personal purpose
            System.out.println(msg);
            System.out.println(channel);
            System.out.println(msg.getAuthor());

            //Gathering all names from text file and Creating ArrayList to store SummonerLeague instances
            List<String> raceNames = readFileInList("src/main/java/race.txt");

            ArrayList<SummonerLeague> summoners = new ArrayList<>();

            //create instances of SummonerLeague and add to summoners Arraylist
//            for (String name : raceNames) {
//                try {
//                    System.out.println("Trying: " + name);
//                    summoners.add(createSummonerLeague(name, getPuuidFromName(name)));
//                } catch (RiotApiException | IOException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }

            raceNames.parallelStream().forEach(name->{
                try {
                    System.out.println("Trying: " + name);
                    String[] values = name.split(",");
                    String summonerName = values[0];
                    int lastStreak = Integer.parseInt(values[1]);
                    String lastMatchId = values[2];
                    summoners.add(createSummonerLeague(summonerName,getPuuidFromName(summonerName),lastStreak,lastMatchId));
                   // summoners.add(createSummonerLeague(name, getPuuidFromName(name)));
                } catch(Exception e){
                    msg.getChannel().sendMessage("Error in processing information for \""+ name +"\"").queue();
                }
            });

            //Sorting the Arraylist of SummonerLeague's by rank
            sortSummonersLeague(summoners);

            //Getting the hottest hot streak and coldest cold streak as a summoner
            SummonerLeague max = Collections.max(summoners, Comparator.comparingInt(SummonerLeague::getStreak));
            SummonerLeague min = Collections.min(summoners, Comparator.comparingInt(SummonerLeague::getStreak));

            //Create the String to build for full message
            //Example of finalString https://postimg.cc/JtvRswcV
            StringBuilder finalString = new StringBuilder(100);

            //Depending on which streak is bigger display in order, and only display if streak is equal or greater than 2
            if (max.getStreak() < Math.abs(min.getStreak())) {
                if (min.getStreak() <= -2) {
                    finalString.append("\uD83E\uDD76 ").append(min.getNickName()).append(" is on a ").append(Math.abs(min.getStreak())).append(" game loss streak \uD83E\uDD76");
                }
                finalString.append("\n");
                if (max.getStreak() >= 2) {
                    finalString.append("\uD83D\uDD25 ").append(max.getNickName()).append(" is on a ").append(max.getStreak()).append(" game win streak \uD83D\uDD25");
                }
            } else {
                if (max.getStreak() >= 2) {
                    finalString.append("\uD83D\uDD25 ").append(max.getNickName()).append(" is on a ").append(max.getStreak()).append(" game win streak \uD83D\uDD25");
                }
                finalString.append("\n");
                if (min.getStreak() <= -2) {
                    finalString.append("\uD83E\uDD76 ").append(min.getNickName()).append(" is on a ").append(Math.abs(min.getStreak())).append(" game loss streak \uD83E\uDD76");
                }
            }

            //Adding spacing
            finalString.append("\n\n");

            //Display each summoner's information
            for (SummonerLeague summoner : summoners) {
                finalString.append(summoner.toString());
                if (summoner.equals(summoners.get(summoners.size() - 1)) && summoner.getName().equalsIgnoreCase("Jaçob")) {
                    finalString.append("<:OkaygeBusiness:889569967673049118>");
                } else if(summoner.equals(summoners.get(summoners.size() - 1))){
                    finalString.append("<:sadge:726604997202149457>");
                }
                finalString.append("\n");
            }

            //Send final string to channel
            channel.sendMessage(finalString).queue();

            //Send system message successful
            System.out.println("$race was successful");
            System.out.println(finalString);
            return;
        }
        //Make sure race isn't called too often
        else if (msg.getContentRaw().equals("$race") && !(msg.getAuthor().isBot()) && !(inputDelay())) {
            Random rand = new Random();
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();

            //Display on console for personal purpose
            System.out.println(msg);
            System.out.println(channel);
            System.out.println(msg.getAuthor());
            int coolDown = 60;
            long now = System.currentTimeMillis();
            int seconds = (int) (coolDown - ((now - lastTime)/1000));


            int random = rand.nextInt(5);
            switch (random) {
                case 0:
                    channel.sendMessage("Whoa summoner! I doubt Mich is getting a \"coin flip\" jungle that soon. \nWait for " + seconds + " more seconds" ).queue();
                    break;
                case 1:
                    channel.sendMessage("Whoa summoner! Niv is for sure still the third ranked support in the server...I promise.\nWait for " + seconds + " more seconds").queue();
                    break;
                case 2:
                    channel.sendMessage("Whoa summoner! Naweed is definitely not THAT high tempo.\nWait for " + seconds + " more seconds").queue();
                    break;
                case 3:
                    channel.sendMessage("I hate naweed...and you said $race too quick.\nWait for " + seconds + " more seconds").queue();
                    break;
                case 4:
                    channel.sendMessage("Can you stop calling $race so often you piss random.\nWait for " + seconds + " more seconds").queue();
                    break;
                default:
                    break;
            }
            return;
        }

        /*
            $race add "playerName" - adds a player to race
         */
        if (message.startsWith("$race add") && !(msg.getAuthor().isBot()) && msg.getMember().getRoles().contains(role)) {
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();
            message = message.substring(9).trim();
            message = message.toLowerCase(Locale.ROOT);

            //Display on console for personal purpose
            System.out.println(msg);
            System.out.println(channel);
            System.out.println(msg.getAuthor());

            try {
                String summonerId = getSummonerIdFromName(message);
            } catch (Exception e) {
                channel.sendMessage("This is not a valid summoner name").queue();
                e.printStackTrace();
                return;
            }

            try {
                FileWriter myWriter = new FileWriter("src/main/java/race.txt", true);
                myWriter.write("\n" + message +",0,N/A");
                myWriter.close();
                System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            File file1 = new File("src/main/java/race.txt");
            removeEmptyLines(file1);
            return;
        } else if (message.startsWith("$race add") && !(msg.getAuthor().isBot()) && !(msg.getMember().getRoles().contains(role))) {
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();
            sendNonPermissionMessage(channel);
            return;
        }

        /*
            $race remove "playerName" - removes a player from the race
         */
        if (message.startsWith("$race remove") && !(msg.getAuthor().isBot()) && msg.getMember().getRoles().contains(role)) {
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();
            message = message.substring(12).trim();
            message = message.toLowerCase(Locale.ROOT);

            //Display on console for personal purpose
            System.out.println(msg);
            System.out.println(channel);
            System.out.println(msg.getAuthor());


            File inputFile = new File("src/main/java/race.txt");
            File tempFile = new File("myTempFile.txt");

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(inputFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(tempFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

            String lineToRemove = message;
            String currentLine = null;

            while (true) {
                try {
                    if (!((currentLine = reader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.startsWith(lineToRemove)) continue;
                try {
                    writer.write(currentLine + System.getProperty("line.separator"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            boolean successful = tempFile.renameTo(inputFile);
            System.out.println(successful);

            removeEmptyLines(inputFile);
            return;
        } else if (message.startsWith("$race remove") && !(msg.getAuthor().isBot()) && !(msg.getMember().getRoles().contains(role))) {
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();

            //Display on console for personal purpose
            System.out.println(msg);
            System.out.println(channel);
            System.out.println(msg.getAuthor());

            sendNonPermissionMessage(channel);
            return;
        }


        /*
            $help - displays all the commands and disclaimer
         */
        if (message.equalsIgnoreCase("$help") && !(msg.getAuthor().isBot())) {
            MessageChannel channel = event.getChannel();
            channel.sendTyping().queue();

            //Display on console for personal purpose
            System.out.println(msg);
            System.out.println(channel);
            System.out.println(msg.getAuthor());

            StringBuilder finalString = new StringBuilder(100);
            finalString.append("__**Welcome to the Help Page for Gunzir Race Bot.**__");
            finalString.append("\n\n\n");
            finalString.append("**$race** - shows the current league race");
            finalString.append("\n\n");
            finalString.append("**$race add** ***PlayerName*** - adds PlayerName to the race");
            finalString.append("\n\n");
            finalString.append("**$race remove** ***PlayerName*** - removes PlayerName from the race");
            finalString.append("\n\n");
            finalString.append("**$racetft** - shows the current TFT race");
            finalString.append("\n\n");
            finalString.append("**$racetft add** ***PlayerName*** - adds PlayerName to the TFT race");
            finalString.append("\n\n");
            finalString.append("**$racetft remove** ***PlayerName*** - removes PlayerName from the TFT race");
            finalString.append("\n\n\n *Disclaimer: Gunzir Racing Bot isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games, and all associated properties are trademarks or registered trademarks of Riot Games, Inc.*");

            channel.sendMessage(finalString).queue();
            return;
        }


    }



    /*
    =======================================================================================================================
    STATIC METHODS STATIC METHODS STATIC METHODS STATIC METHODS STATIC METHODS STATIC METHODS STATIC METHODS STATIC METHODS
    =======================================================================================================================
     */

    public static Summoner getSummonerFromName(String name) throws RiotApiException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("accept", "application/json")
                .uri(URI.create(getSummonerHTTP(name)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Summoner summoner = mapper.readValue(response.body(), Summoner.class);

        return summoner;
    }

    public static String getSummonerIdFromName(String name) throws RiotApiException, IOException, InterruptedException {
        String id = "";
        Summoner summoner = null;
        for (int i = 0; i < 25; i++) {
            try{
                summoner = getSummonerFromName(name);
            } catch(Exception e){
                System.out.println(name + " getSummonerFromName; Attempt #" + i);
                continue;
            }
            break;
        }

        if(summoner.equals(null)) throw new NullPointerException("Summoner not returning, equal to null");

        for (int i = 0; i < 25; i++) {
            try{
                 id= summoner.getId();
            } catch(Exception e){
                System.out.println(name + " getSummonerIdFromName; Attempt #" + i);
                continue;
            }
            break;
        }

        if(id.equals("")) throw new NullPointerException("Id not returning, equal to \"\"");

        return id;
    }

    public static String getPuuidFromName(String name) throws RiotApiException, IOException, InterruptedException {
        String puuid = "";
        Summoner summoner = null;
        for (int i = 0; i < 25; i++) {
            try{
                summoner = getSummonerFromName(name);
            } catch(Exception e){
                System.out.println(name + " getSummonerFromName; Attempt #" + i);
                continue;
            }
            break;
        }

        if(summoner.equals(null)) throw new NullPointerException("Summoner not returning, equal to null");

        for (int i = 0; i < 25; i++) {
            try{
                puuid= summoner.getPuuid();
            } catch(Exception e){
                System.out.println(name + " getPuuidFromName; Attempt #" + i);
                continue;
            }
            break;
        }

        if(puuid.equals("")) throw new NullPointerException("Puuid not returning, equal to \"\"");

        return puuid;
    }

    public static String getSummonerHTTP(String name) {
        if(name.contains(" ")){
            name = name.replace(" ", "%20");
        }
        return "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/"
                + name
                + "?api_key="
                + riotApiKey;
    }

    public static List<String> getMatchlistFromName(String name) throws RiotApiException, IOException, InterruptedException {
        String puuid = getPuuidFromName(name);
        System.out.println(puuid);
        List<String> list = null;

        for (int i = 0; i < 400; i++) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .header("accept", "application/json")
                    .uri(URI.create(getMatchesURL(puuid)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Gson converter = new Gson();
            Type type = new TypeToken<List<String>>(){}.getType();

            try{
                list = converter.fromJson(response.body(), type );
            } catch(Exception e){
                System.out.println(name + " getMatchlistFromName; Attempt #" + i);
                continue;
            }

            return list;
        }
        return list;
    }

    public static String getMatchesURL (String puuid){
        return RIOT_API_STRING + "/lol/match/v5/matches/by-puuid/"
                + puuid
                + "/ids?queue=420&start=0&count=20&api_key="
                + riotApiKey;
    }

    public static boolean inputDelay () {
        double now = System.currentTimeMillis();
        double coolDownInMillis = 60000;
        if (now - lastTime > coolDownInMillis) {
            //do what you want
            System.out.printf("\nResult: %.2f\tCooldown: %.2f", now - lastTime, coolDownInMillis);
            System.out.print("\nTrue");
            lastTime = System.currentTimeMillis();
            return true;
        } else {
            System.out.printf("\nResult: %.2f\tCooldown: %.2f", now - lastTime, coolDownInMillis);
            System.out.print("\nFalse");
            return false;
        }
    }

    public static @NotNull String readFileAsString (String fileName) throws Exception {
        String data;
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    public static int getStreakCount (List < String > matchList, String summonerName,int lastStreak, String lastMatchId) throws
    IOException, InterruptedException {


        int streak = 0;

        for (String matchID : matchList) {
            if(matchID.equals(lastMatchId)){
                if( (streak > 0 && lastStreak > 0) || (streak < 0 && lastStreak < 0) ){
                    streak = streak + lastStreak;
                } else if( streak ==0){
                    streak = lastStreak;
                }

                lastStreak = streak;
                lastMatchId = matchList.get(0);
                writeSummonerTextFile(summonerName, lastStreak, lastMatchId);
                return streak;
            }
            boolean win = getWinFromMatchID(matchID, summonerName);
            if (streak == 0) {
                if (win) {
                    streak++;
                } else {
                    streak--;
                }
            } else if (streak < 0) {
                if (!win) {
                    streak--;
                } else {
                    break;
                }
            } else {
                if (win) {
                    streak++;
                } else {
                    break;
                }
            }
        }
        lastStreak = streak;
        lastMatchId = matchList.get(0);
        writeSummonerTextFile(summonerName, lastStreak, lastMatchId);
        return streak;
    }

    private static void writeSummonerTextFile(String summonerName, int lastStreak, String lastMatchId) throws IOException {
        List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get("src/main/java/race.txt"), StandardCharsets.UTF_8));

        for (int i = 0; i < fileContent.size(); i++) {
            System.out.println(fileContent.get(i));
            if (fileContent.get(i).startsWith(summonerName)) {
                fileContent.set(i, summonerName+","+lastStreak+","+lastMatchId);
                break;
            }
        }

        Files.write(Paths.get("src/main/java/race.txt"), fileContent, StandardCharsets.UTF_8);
    }

    public static boolean getWinFromMatchID (String matchID, String summonerName) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        System.out.println(summonerName + ": " + matchID);
        if (!matchID.startsWith("NA")) {
            throw new InvalidParameterException("Invalid matchID: \"" + matchID + "\" - check API");
        }
        ArrayList<Participant> participants = null;

        for (int i = 0; i < 250; i++) {
            TimeUnit.SECONDS.sleep(1);
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .header("accept", "application/json")
                    .uri(URI.create(getMatchURL(matchID)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            Game game = mapper.readValue(response.body(), Game.class);
            try{
                participants = game.getInfo().getParticipants();
            } catch(Exception e){
                System.out.println(summonerName + " - getWinFromMatchID: " + matchID + "; Attempt #" + i);
                continue;
            }
            break;
        }

        if(participants.isEmpty()){
            throw new NullPointerException("getWinFromMatchID failed.");
        }

        for (int i = 0; i < 10; i++) {
            if (participants.get(i).getSummonerName().equalsIgnoreCase(summonerName)) {
                return participants.get(i).isWin();
            }
        }

        System.out.println("Returning false in " + matchID + "; for " + summonerName);
        return false;
    }

    public static String getMatchURL (String matchID){
        return RIOT_API_STRING + "/lol/match/v5/matches/"
                + matchID
                + "?api_key="
                + riotApiKey;
    }

    public static SummonerLeague createSummonerLeague (String summonerName, String puuid, int lastStreak, String lastMatchId) throws
    RiotApiException, IOException, InterruptedException {

        Set<LeagueEntry> summonerLeagues = null;
        try {
            summonerLeagues = api.getLeagueEntriesBySummonerId(Platform.NA, getSummonerIdFromName(summonerName));
        } catch (RiotApiException e) {
            e.printStackTrace();
        }
        List<String> matchList = getMatchlistFromName(summonerName);
        int streak = getStreakCount(matchList, summonerName, lastStreak, lastMatchId);

        SummonerLeague summoner = null;
        assert summonerLeagues != null;
        for (LeagueEntry league : summonerLeagues) {
            if (league.getQueueType().equals("RANKED_SOLO_5x5")) {
                if (league.getLeaguePoints() == 100) {
                    summoner = new SummonerLeague(league.getSummonerName(), league.getRank(), league.getLeaguePoints(), league.getTier(), streak, league.getMiniSeries());
                } else {
                    summoner = new SummonerLeague(league.getSummonerName(), league.getRank(), league.getLeaguePoints(), league.getTier(), streak);
                }
            }
        }

        return summoner;
    }

    public static void sortSummonersLeague (@NotNull ArrayList < SummonerLeague > arrayList) {
        arrayList.sort((o1, o2) -> o2.getLP() - o1.getLP());

        arrayList.sort(Comparator.comparingInt(SummonerLeague::getRealDivision));

        arrayList.sort(Collections.reverseOrder(Comparator.comparingInt(SummonerLeague::getWins)));

        arrayList.sort(Comparator.comparingInt(SummonerLeague::getRealTier));
    }

    public static List<String> readFileInList (String fileName){
        List<String> lines = Collections.emptyList();
        try {
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void sendNonPermissionMessage (MessageChannel channel){
        channel.sendMessage("You do not have permission to use this command.").queue();
    }

    public static void removeEmptyLines (File file1){
        Scanner file;
        PrintWriter writer;

        try {

            File file2 = new File("src/main/java/myTempFile.txt");

            file = new Scanner(file1);
            writer = new PrintWriter(file2);

            while (file.hasNext()) {
                String line = file.nextLine();
                if (!line.isEmpty()) {
                    writer.write(line);
                    writer.write("\n");
                }
            }

            file.close();
            writer.close();

            file1.delete();
            file2.renameTo(file1);


        } catch (FileNotFoundException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


