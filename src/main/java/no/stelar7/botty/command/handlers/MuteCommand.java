package no.stelar7.botty.command.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import no.stelar7.botty.command.*;
import no.stelar7.botty.database.SettingsConnection;
import no.stelar7.botty.listener.Listener;
import no.stelar7.botty.utils.*;
import org.slf4j.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Listener(events = {MemberJoinEvent.class})
public class MuteCommand extends Command
{
    private final Logger              logger = LoggerFactory.getLogger(MuteCommand.class);
    private final FileMappedSettings  file   = new FileMappedSettings(SettingsUtil.getJarFolder().resolve("muted.json"));
    private       Map<String, String> mutes  = new ConcurrentHashMap<>();
    private final Snowflake           muteRoleId;
    private final Snowflake           apiGuildId;
    
    public MuteCommand(GatewayDiscordClient client)
    {
        //this.setAdminOnly(true);
        this.mutes = file.getMap("mutes");
        muteRoleId = Snowflake.of(SettingsConnection.INSTANCE.getSetting("muteRoleId"));
        apiGuildId = Snowflake.of(SettingsConnection.INSTANCE.getSetting("apiGuildId"));
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> checkIfShouldUnmute(client), 0, 5, TimeUnit.MINUTES);
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("mute", "unmute", "muteduration");
    }
    
    @Override
    public void execute(CommandParameters params)
    {
        Set<Snowflake>       mentionedUsers = params.getMessage().getUserMentionIds();
        TextChannel          channel        = (TextChannel) params.getMessage().getChannel().block();
        Guild                guild          = params.getMessage().getGuild().block();
        GatewayDiscordClient client         = params.getMessage().getClient();
        
        if (params.getCommand().equalsIgnoreCase("mute"))
        {
            handleMute(mentionedUsers, channel, guild, client);
        }
        
        if (params.getCommand().equalsIgnoreCase("unmute"))
        {
            handleUnmute(mentionedUsers, channel, guild, client);
        }
        
        if (params.getCommand().equalsIgnoreCase("muteduration"))
        {
            handleMuteDuration(channel);
        }
    }
    
    private void handleMuteDuration(TextChannel channel)
    {
        long now = DateUtils.dateToEpochMillis(DateUtils.dateNow());
        
        List<String> listing = new ArrayList<>();
        this.mutes.forEach((k, v) -> {
            long     endTime  = Long.parseLong(v);
            Duration duration = Duration.ofMillis(endTime).minusMillis(now);
            listing.add(MentionUtil.user(k) + " - " + duration.getSeconds() + " seconds remaining");
        });
        
        if (listing.size() == 0)
        {
            channel.createMessage("No users are muted at the moment").block();
        } else
        {
            channel.createMessage(String.join("\n", listing)).block();
        }
    }
    
    private void handleUnmute(Set<Snowflake> mentionedUsers, TextChannel channel, Guild guild, GatewayDiscordClient client)
    {
        boolean didUpdate = false;
        for (Snowflake flake : mentionedUsers)
        {
            if (mutes.remove(flake.asString()) != null)
            {
                User user = client.getUserById(flake).block();
                RoleUtils.addRoleIfMember(muteRoleId, user, guild);
                channel.createMessage(MentionUtil.user(user.getId()) + " is now unmuted").block();
                didUpdate = true;
            }
        }
        
        if (didUpdate)
        {
            file.putMap("mutes", mutes);
        }
    }
    
    private void handleMute(Set<Snowflake> mentionedUsers, TextChannel channel, Guild guild, GatewayDiscordClient client)
    {
        for (Snowflake flake : mentionedUsers)
        {
            mutes.put(flake.asString(), DateUtils.dateToEpochMillisString(DateUtils.dateNow().plusDays(1)));
            User user = client.getUserById(flake).block();
            RoleUtils.addRoleIfMember(muteRoleId, user, guild);
            channel.createMessage(MentionUtil.user(user.getId()) + " is now muted").block();
        }
        
        file.putMap("mutes", mutes);
    }
    
    public void onJoin(MemberJoinEvent event)
    {
        if (mutes.containsKey(event.getMember().getId().asString()))
        {
            event.getMember().addRole(muteRoleId).block();
            logger.info("Added mute to " + event.getMember().getUsername());
        }
    }
    
    private void checkIfShouldUnmute(GatewayDiscordClient client)
    {
        long                now       = DateUtils.dateToEpochMillis(DateUtils.dateNow());
        Map<String, String> clone     = new HashMap<>(this.mutes);
        AtomicBoolean       hadChange = new AtomicBoolean(false);
        clone.forEach((k, v) -> {
            long     endTime  = Long.parseLong(v);
            Duration duration = Duration.ofMillis(endTime).minusMillis(now);
            
            if (duration.isNegative())
            {
                hadChange.set(true);
                this.mutes.remove(k);
                User  user  = client.getUserById(Snowflake.of(k)).block();
                Guild guild = client.getGuildById(this.apiGuildId).block();
                RoleUtils.removeRoleIfMember(muteRoleId, user, guild);
                logger.info("Removed mute from " + user.getUsername());
            }
        });
        
        if (hadChange.get())
        {
            file.putMap("mutes", mutes);
        }
    }
}
