package no.stelar7.botty.utils;

import discord4j.core.object.*;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.rest.util.PermissionSet;

import java.util.Optional;

public class PermissionUtil
{
    public static PermissionOverwrite createFor(Role role, PermissionSet allowed, PermissionSet denied)
    {
        return PermissionOverwrite.forRole(role.getId(), allowed, denied);
    }
    
    public static PermissionOverwrite createFor(Member member, PermissionSet allowed, PermissionSet denied)
    {
        return PermissionOverwrite.forMember(member.getId(), allowed, denied);
    }
    
    public static PermissionOverwrite getOverrideFor(GuildChannel channel, Role role)
    {
        PermissionOverwrite defaultPermissions = PermissionOverwrite.forRole(role.getId(), PermissionSet.none(), PermissionSet.none());
        
        Optional<ExtendedPermissionOverwrite> current = channel.getOverwriteForRole(role.getId());
        if (current.isPresent())
        {
            return current.get();
        }
        
        return defaultPermissions;
    }
    
    public static PermissionOverwrite allow(Role role, PermissionOverwrite current, PermissionSet change)
    {
        return PermissionUtil.createFor(role, add(current.getAllowed(), change), remove(current.getDenied(), change));
    }
    
    public static PermissionOverwrite deny(Role role, PermissionOverwrite current, PermissionSet change)
    {
        return PermissionUtil.createFor(role, remove(current.getAllowed(), change), add(current.getDenied(), change));
    }
    
    public static PermissionOverwrite unset(Role role, PermissionOverwrite current, PermissionSet change)
    {
        return PermissionUtil.createFor(role, remove(current.getAllowed(), change), remove(current.getDenied(), change));
    }
    
    public static PermissionSet add(PermissionSet current, PermissionSet added)
    {
        return current.or(added);
    }
    
    public static PermissionSet remove(PermissionSet current, PermissionSet removed)
    {
        return current.and(removed.not());
    }
    
    public static boolean isAllowed(PermissionOverwrite permissions, PermissionSet change)
    {
        return permissions.getAllowed().and(change).equals(change);
    }
    
    public static boolean isDenied(PermissionOverwrite permissions, PermissionSet change)
    {
        return permissions.getDenied().and(change).equals(change);
    }
}
