package no.stelar7.botty.utils;

import discord4j.common.util.Snowflake;

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
    
    public static String user(String id)
    {
        return "<@" + id + ">";
    }
    
    public static String user(Snowflake id)
    {
        return user(id.asString());
    }
    
    public static String channel(String id)
    {
        return "<#" + id + ">";
    }
    
    public static String channel(Snowflake id)
    {
        return channel(id.asString());
    }
}
