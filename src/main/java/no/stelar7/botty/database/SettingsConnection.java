package no.stelar7.botty.database;

import no.stelar7.botty.utils.SQLUtils;

import java.sql.*;
import java.util.*;

public class SettingsConnection extends BotConnection
{
    private static final String             table                        = "settings";
    private static final String             SETTING_OFFICE_HOURS_CHANNEL = "office-hours-channel";
    public static final  SettingsConnection INSTANCE                     = new SettingsConnection();
    
    
    private SettingsConnection()
    {
        super();
        
        List<String> columns = List.of(
                "`id` INT unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY,",
                "`setting` VARCHAR(255) NOT NULL CHARACTER SET utf8mb4 COLLATE utf8mb4_swedish_ci",
                "`value` VARCHAR(2000) NOT NULL CHARACTER SET utf8mb4 COLLATE utf8mb4_swedish_ci"
                                      );
        
        this.createTableIfMissing(table, columns);
    }
    
    public String getOfficeHoursChannel()
    {
        try
        {
            ResultSet rs = this.select(table, "*", "WHERE " + SQLUtils.wrapInSQLQuotes("setting") + "=" + SETTING_OFFICE_HOURS_CHANNEL);
            rs.first();
            return rs.getString("value");
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
}
