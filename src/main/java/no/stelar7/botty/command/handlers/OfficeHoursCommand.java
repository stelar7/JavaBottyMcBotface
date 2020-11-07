package no.stelar7.botty.command.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.object.*;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.*;
import no.stelar7.botty.command.*;
import no.stelar7.botty.utils.PermissionUtil;

import java.util.*;

public class OfficeHoursCommand extends Command
{
    
    private static TextChannel channel;
    private static Role        everyone;
    
    
    @Override
    public void execute(CommandParameters params)
    {
        if (channel == null)
        {
            channel = (TextChannel) params.getMessage().getGuild().block()
                                          .getChannelById(Snowflake.of(774418825512747079L)).block();
        }
        
        if (everyone == null)
        {
            everyone = params.getMessage().getGuild().block().getEveryoneRole().block();
        }
        
        PermissionSet       change      = PermissionSet.of(Permission.SEND_MESSAGES);
        PermissionOverwrite permissions = PermissionUtil.getOverrideFor(channel, everyone);
        
        if (params.getCommand().equalsIgnoreCase("open"))
        {
            permissions = PermissionUtil.allow(everyone, permissions, change);
            channel.createMessage("open").block();
        }
        
        if (params.getCommand().equalsIgnoreCase("close"))
        {
            permissions = PermissionUtil.deny(everyone, permissions, change);
            channel.createMessage("close").block();
        }
        
        channel.addRoleOverwrite(everyone.getId(), permissions).block();
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("open", "close");
    }
}
