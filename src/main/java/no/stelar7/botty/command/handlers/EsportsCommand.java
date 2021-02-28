package no.stelar7.botty.command.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.Color;
import no.stelar7.botty.command.*;
import no.stelar7.botty.database.SettingsConnection;
import no.stelar7.botty.utils.*;
import org.slf4j.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EsportsCommand extends Command
{
    public static final  Logger                                           logger   = LoggerFactory.getLogger(EsportsCommand.class);
    private static final Map<LocalDate, Map<String, List<EventGameData>>> schedule = new HashMap<>();
    
    private final TextChannel esportsChannel;
    
    public EsportsCommand(GatewayDiscordClient client)
    {
        Snowflake esportsChannelId = Snowflake.of(SettingsConnection.INSTANCE.getESportsChannel());
        esportsChannel = (TextChannel) client.getChannelById(esportsChannelId).block();
        
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::updateMatchData, 0, 1, TimeUnit.DAYS);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this::postUpdateMessage, 0, 12, TimeUnit.HOURS);
    }
    
    public void sendEmbed(Map<String, List<EventGameData>> entries, LocalDateTime date)
    {
        if (entries.size() == 0)
        {
            esportsChannel.createMessage("No games played on " + date.toLocalDate().toString());
            return;
        }
        
        esportsChannel.createEmbed(embed -> {
            embed.setTitle("Games being played on " + date.toLocalDate().toString());
            embed.setColor(Color.of(0x9b311a));
            
            entries.forEach((league, games) -> {
                StringJoiner sj = new StringJoiner("\n");
                games.forEach(game -> {
                    Duration timeToGame = Duration.between(LocalDateTime.now(), game.getTime());
                    String prettyTime = timeToGame.toString()
                                                  .substring(2)
                                                  .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                                                  .replaceAll("\\.\\d+", "")
                                                  .toLowerCase();
                    
                    sj.add(game.getTeamA() + " vs " + game.getTeamB() + ", starting in " + prettyTime);
                });
                
                if (sj.length() == 0)
                {
                    return;
                }
                
                sj.add(StringUtils.toUrl("More about " + league + " here", "https://lolesports.com/schedule?leagues=" + games.get(0).getUrl()));
                
                embed.addField(league, sj.toString(), false);
            });
        }).block();
    }
    
    public Map<String, List<EventGameData>> filterDataToDateRange(LocalDateTime past, LocalDateTime future)
    {
        Map<String, List<EventGameData>> printedGames = new HashMap<>();
        schedule.forEach((date, leagues) -> {
            if (date.isAfter(future.toLocalDate()))
            {
                return;
            }
            
            leagues.forEach((league, games) -> games.forEach(game -> {
                if (game.getTime().isBefore(past))
                {
                    return;
                }
                
                printedGames.putIfAbsent(league, new ArrayList<>());
                printedGames.get(league).add(game);
            }));
        });
        
        return printedGames;
    }
    
    public void postUpdateMessage()
    {
        try
        {
            LocalDateTime now      = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);
            
            sendEmbed(filterDataToDateRange(now, tomorrow), now);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void updateMatchData()
    {
        try
        {
            Map<String, String>             requestHeaders     = Map.of("x-api-key", "0TvQnueqKa5mxJntVWt0w4LpLfEkrV1Ta8rQBb9Z");
            String                          leagueListUrl      = "https://esports-api.lolesports.com/persisted/gw/getLeagues?hl=en-US";
            String                          leagueListResponse = HttpUtils.doGetRequest(leagueListUrl, requestHeaders);
            LeagueSummaryContainerContainer leagueListData     = StringUtils.toType(leagueListResponse, LeagueSummaryContainerContainer.class);
            String                          leagueIds          = leagueListData.getData().getLeagues().stream().map(LeagueSummary::getId).collect(Collectors.joining(","));
            
            String                                  eventListUrl      = "https://esports-api.lolesports.com/persisted/gw/getEventList?hl=en-US&leagueId=" + leagueIds;
            String                                  eventListResponse = HttpUtils.doGetRequest(eventListUrl, requestHeaders);
            EsportsEventContainerContainerContainer eventData         = StringUtils.toType(eventListResponse, EsportsEventContainerContainerContainer.class);
            
            List<EsportsEvent> events = eventData.getData().getEsports().getEvents();
            events.forEach(event -> {
                EventGameData entry = new EventGameData(
                        event.getLeague().getName(),
                        event.getLeague().getSlug(),
                        ZonedDateTime.parse(event.getStartTime()).toLocalDateTime(),
                        event.getMatch().getTeams().get(0).getCode(),
                        event.getMatch().getTeams().get(1).getCode()
                );
                
                schedule.putIfAbsent(entry.getTime().toLocalDate(), new HashMap<>());
                schedule.get(entry.getTime().toLocalDate()).putIfAbsent(entry.getLeague(), new ArrayList<>());
                schedule.get(entry.getTime().toLocalDate()).get(entry.getLeague()).add(entry);
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void execute(CommandParameters params)
    {
        if (!params.getMessage().getChannel().block().getId().equals(esportsChannel.getId()))
        {
            params.getMessage().getChannel().block().createMessage("To avoid spoilers, this command is restricted to " + MentionUtil.channel(esportsChannel.getId())).block();
            return;
        }
        
        LocalDateTime now      = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        sendEmbed(filterDataToDateRange(now, tomorrow), now);
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("esports");
    }
    
    @Override
    public boolean isDisabled()
    {
        return false;
    }
    
    static class EventGameData
    {
        String        league;
        String        url;
        LocalDateTime time;
        String        teamA;
        String        teamB;
        
        public EventGameData(String league, String slug, LocalDateTime time, String teamA, String teamB)
        {
            this.league = league;
            this.url = slug;
            this.time = time;
            this.teamA = teamA;
            this.teamB = teamB;
        }
        
        public String getLeague()
        {
            return league;
        }
        
        public String getUrl()
        {
            return url;
        }
        
        public LocalDateTime getTime()
        {
            return time;
        }
        
        public String getTeamA()
        {
            return teamA;
        }
        
        public String getTeamB()
        {
            return teamB;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            EventGameData that = (EventGameData) o;
            return Objects.equals(league, that.league) && Objects.equals(url, that.url) && Objects.equals(time, that.time) && Objects.equals(teamA, that.teamA) && Objects.equals(teamB, that.teamB);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(league, url, time, teamA, teamB);
        }
        
        @Override
        public String toString()
        {
            return "EventGameData{" +
                   "league='" + league + '\'' +
                   ", url='" + url + '\'' +
                   ", time=" + time +
                   ", teamA='" + teamA + '\'' +
                   ", teamB='" + teamB + '\'' +
                   '}';
        }
    }
    
    static class LeagueSummaryContainerContainer
    {
        LeagueSummaryContainer data;
        
        public LeagueSummaryContainer getData()
        {
            return data;
        }
    }
    
    static class LeagueSummaryContainer
    {
        List<LeagueSummary> leagues;
        
        public List<LeagueSummary> getLeagues()
        {
            return leagues;
        }
    }
    
    static class LeagueSummary
    {
        String id;
        String slug;
        String name;
        String region;
        String image;
        int    priority;
        
        public String getId()
        {
            return id;
        }
        
        public String getSlug()
        {
            return slug;
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getRegion()
        {
            return region;
        }
        
        public String getImage()
        {
            return image;
        }
        
        public int getPriority()
        {
            return priority;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            LeagueSummary that = (LeagueSummary) o;
            return priority == that.priority && Objects.equals(id, that.id) && Objects.equals(slug, that.slug) && Objects.equals(name, that.name) && Objects.equals(region, that.region) && Objects.equals(image, that.image);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(id, slug, name, region, image, priority);
        }
        
        @Override
        public String toString()
        {
            return "LeagueSummary{" +
                   "id='" + id + '\'' +
                   ", slug='" + slug + '\'' +
                   ", name='" + name + '\'' +
                   ", region='" + region + '\'' +
                   ", image='" + image + '\'' +
                   ", priority=" + priority +
                   '}';
        }
    }
    
    static class EsportsEventContainerContainerContainer
    {
        EsportsEventContainerContainer data;
        
        public EsportsEventContainerContainer getData()
        {
            return data;
        }
    }
    
    static class EsportsEventContainerContainer
    {
        EsportsEventContainer esports;
        
        public EsportsEventContainer getEsports()
        {
            return esports;
        }
    }
    
    static class EsportsEventContainer
    {
        List<EsportsEvent> events;
        
        public List<EsportsEvent> getEvents()
        {
            return events;
        }
    }
    
    static class EsportsEvent
    {
        String        startTime;
        EsportsMatch  match;
        EsportsLeague league;
        
        public String getStartTime()
        {
            return startTime;
        }
        
        public EsportsMatch getMatch()
        {
            return match;
        }
        
        public EsportsLeague getLeague()
        {
            return league;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            EsportsEvent that = (EsportsEvent) o;
            return Objects.equals(startTime, that.startTime) && Objects.equals(match, that.match) && Objects.equals(league, that.league);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(startTime, match, league);
        }
        
        @Override
        public String toString()
        {
            return "EsportsEvent{" +
                   "startTime='" + startTime + '\'' +
                   ", match=" + match +
                   ", league=" + league +
                   '}';
        }
    }
    
    static class EsportsLeague
    {
        String id;
        String slug;
        String name;
        
        public String getId()
        {
            return id;
        }
        
        public String getSlug()
        {
            return slug;
        }
        
        public String getName()
        {
            return name;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            EsportsLeague that = (EsportsLeague) o;
            return Objects.equals(id, that.id) && Objects.equals(slug, that.slug) && Objects.equals(name, that.name);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(id, slug, name);
        }
        
        @Override
        public String toString()
        {
            return "EsportsLeague{" +
                   "id='" + id + '\'' +
                   ", slug='" + slug + '\'' +
                   ", name='" + name + '\'' +
                   '}';
        }
    }
    
    static class EsportsMatch
    {
        List<EsportsTeam> teams;
        
        public List<EsportsTeam> getTeams()
        {
            return teams;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            EsportsMatch that = (EsportsMatch) o;
            return Objects.equals(teams, that.teams);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(teams);
        }
        
        @Override
        public String toString()
        {
            return "EsportsMatch{" +
                   "teams=" + teams +
                   '}';
        }
    }
    
    static class EsportsTeam
    {
        String code;
        String image;
        
        public String getCode()
        {
            return code;
        }
        
        public String getImage()
        {
            return image;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            EsportsTeam that = (EsportsTeam) o;
            return Objects.equals(code, that.code) && Objects.equals(image, that.image);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(code, image);
        }
        
        @Override
        public String toString()
        {
            return "EsportsTeam{" +
                   "code='" + code + '\'' +
                   ", image='" + image + '\'' +
                   '}';
        }
    }
}
