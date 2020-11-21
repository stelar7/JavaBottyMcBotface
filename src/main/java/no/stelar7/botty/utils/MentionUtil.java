package no.stelar7.botty.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.Channel;

public class MentionUtil
{
    public static String role(String id)
    {
        return "<@&" + id + ">";
    }
    
    public static String role(Snowflake id)
    {
        return role(id.asString());
    }
    
    public static String role(Role role)
    {
        return role(role.getId());
    }
    
    public static String user(String id)
    {
        return "<@" + id + ">";
    }
    
    public static String user(Snowflake id)
    {
        return user(id.asString());
    }
    
    public static String user(User user)
    {
        return user(user.getId());
    }
    
    public static String channel(String id)
    {
        return "<#" + id + ">";
    }
    
    public static String channel(Snowflake id)
    {
        return channel(id.asString());
    }
    
    public static String channel(Channel channel)
    {
        return channel(channel.getId());
    }
}
