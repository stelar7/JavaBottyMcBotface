package no.stelar7.botty.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.*;

import java.util.Optional;

public class RoleUtils
{
    public static void addRoleIfMember(Snowflake role, User user, Guild guild)
    {
        Optional<Member> member = user.asMember(guild.getId()).blockOptional();
        member.ifPresent(value -> value.addRole(role).block());
    }
    
    public static void removeRoleIfMember(Snowflake role, User user, Guild guild)
    {
        Optional<Member> member = user.asMember(guild.getId()).blockOptional();
        member.ifPresent(value -> value.removeRole(role).block());
    }
}
