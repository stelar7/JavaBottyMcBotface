package no.stelar7.botty.command.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.*;
import no.stelar7.botty.command.*;
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
        this.mutes = SettingsUtil.gson.fromJson(file.getOrDefault("mutes", "{}").toString(), Map.class);
        muteRoleId = SettingsUtil.GLOBAL.getSnowflake("muteRoleId");
        apiGuildId = SettingsUtil.GLOBAL.getSnowflake("apiGuildId");
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
        Set<Snowflake> mentionedUsers = params.getMessage().getUserMentionIds();
        
        if (params.getCommand().equalsIgnoreCase("mute"))
        {
            for (Snowflake flake : mentionedUsers)
            {
                mutes.put(flake.asString(), DateUtils.dateToEpochMillisString(DateUtils.dateNow().plusDays(1)));
                User user = params.getMessage().getClient().getUserById(flake).block();
                RoleUtils.addRoleIfMember(muteRoleId, user, params.getMessage().getGuild().block());
                params.getMessage().getChannel().block().createMessage(user.getUsername() + " is now muted").block();
            }
        }
        
        if (params.getCommand().equalsIgnoreCase("unmute"))
        {
            for (Snowflake flake : mentionedUsers)
            {
                if (mutes.remove(flake.asString()) != null)
                {
                    User user = params.getMessage().getClient().getUserById(flake).block();
                    RoleUtils.addRoleIfMember(muteRoleId, user, params.getMessage().getGuild().block());
                    params.getMessage().getChannel().block().createMessage(user.getUsername() + " is now unmuted").block();
                }
            }
        }
        
        if (params.getCommand().equalsIgnoreCase("muteduration"))
        {
            long now = DateUtils.dateToEpochMillis(DateUtils.dateNow());
            
            List<String> listing = new ArrayList<>();
            for (Snowflake flake : mentionedUsers)
            {
                String end = mutes.get(flake.asString());
                if (end == null)
                {
                    continue;
                }
                
                long     endTime  = Long.parseLong(end);
                Duration duration = Duration.ofMillis(endTime).minusMillis(now);
                listing.add("<@" + flake.asString() + ">: " + duration.getSeconds() + " seconds remaining");
            }
            
            params.getMessage().getChannel().block().createMessage(String.join("\n", listing)).block();
        }
        
        file.put("mutes", SettingsUtil.gson.toJson(mutes));
    }
    
    public void onJoin(MemberJoinEvent event)
    {
        if (mutes.containsKey(event.getMember().getId().asString()))
        {
            event.getMember().addRole(muteRoleId).block();
            logger.info("Added mute to " + event.getMember().getId());
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
                logger.info("Removed mute from " + k);
            }
        });
        
        if (hadChange.get())
        {
            file.put("mutes", SettingsUtil.gson.toJson(mutes));
        }
    }
}
