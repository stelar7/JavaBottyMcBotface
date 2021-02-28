package no.stelar7.botty.database;

import no.stelar7.botty.utils.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsConnection extends BotConnection
{
    public static final  String             table                        = "settings";
    public static final  SettingsConnection INSTANCE                     = new SettingsConnection();
    private static final String             SETTING_OFFICE_HOURS_CHANNEL = "office-hours-channel";
    private static final String             SETTING_ESPORTS_CHANNEL      = "esports-channel";
    
    private SettingsConnection()
    {
        super();
        
        List<String> columns = List.of(
                "`setting` VARCHAR(255) NOT NULL",
                "`value` VARCHAR(2000) NOT NULL"
                                      );
        
        this.createTableIfMissing(table, columns);
    }
    
    public String getOfficeHoursChannel()
    {
        return getSetting(SETTING_OFFICE_HOURS_CHANNEL);
    }
    
    public String getESportsChannel()
    {
        return getSetting(SETTING_ESPORTS_CHANNEL);
    }
    
    public String getSetting(String key)
    {
        try
        {
            ResultSet rs = this.select(table, "*", "WHERE " + SQLUtils.wrapInSQLQuotes("setting") + " = " + SQLUtils.wrapInDoubleQuotes(key));
            if (!rs.first())
            {
                System.out.println("Settings table missing value for key " + key);
                System.exit(0);
            }
            ;
            return rs.getString("value");
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    public List<Pair<String, String>> getAllSettings()
    {
        ResultSet rs = this.select(table, "*", "");
        return SQLUtils.resultSetToList(rs, sql -> {
            try
            {
                return new Pair<>(sql.getString("setting"), sql.getString("value"));
            } catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
            
            return null;
        })
                       .stream()
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
    }
    
    public void putSetting(String key, String value)
    {
        if (this.getSetting(key) == null)
        {
            this.insert(table, List.of("setting", "value"), List.of(key, value));
            return;
        }
        
        this.update(table, List.of("value"), List.of(value), "WHERE " + SQLUtils.wrapInSQLQuotes(key) + " = " + SQLUtils.wrapInDoubleQuotes(key));
    }
    
    public void deleteSetting(String key)
    {
        this.delete(table, "WHERE " + SQLUtils.wrapInSQLQuotes(key) + " = " + SQLUtils.wrapInDoubleQuotes(key));
    }
}
