package no.stelar7.botty.database;

import discord4j.common.util.Snowflake;

import java.sql.*;
import java.util.*;

public class BotConnection extends DatabaseConnection
{
    public BotConnection()
    {
        super();
        
        List<String> columns = List.of(
                "`id` VARCHAR(255) NOT NULL"
                                      );
        
        this.createTableIfMissing("admin_roles", columns);
    }
    
    public List<Snowflake> getAdminRoles()
    {
        try
        {
            List<Snowflake> roles = new ArrayList<>();
            ResultSet    rs    = this.select("admin_roles", "*", "");
            while (rs.next())
            {
                roles.add(Snowflake.of(rs.getString("id")));
            }
            return roles;
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
}
