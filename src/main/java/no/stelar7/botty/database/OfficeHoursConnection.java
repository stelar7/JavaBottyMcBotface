package no.stelar7.botty.database;

import discord4j.core.object.entity.User;
import no.stelar7.botty.command.handlers.OfficeHoursCommand.OfficeHoursQuestion;
import no.stelar7.botty.utils.SQLUtils;

import java.sql.*;
import java.util.*;

public class OfficeHoursConnection extends BotConnection
{
    private final String table = "office-hours";
    
    public OfficeHoursConnection()
    {
        super();
        
        List<String> columns = List.of(
                "`id` INT unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY",
                "`posterSnowflake` VARCHAR(20) NOT NULL COMMENT 'Discord snowflake id'",
                "`answerSnowflake` VARCHAR(20) NOT NULL COMMENT 'Discord snowflake id'",
                "`question` VARCHAR(2000) NOT NULL",
                "`answer` VARCHAR(2000) NOT NULL",
                "`asked` BOOLEAN NOT NULL"
                                      );
        
        this.createTableIfMissing(this.table, columns);
    }
    
    public OfficeHoursQuestion addQuestion(User user, String question)
    {
        int insertedId = this.insert(
                this.table,
                List.of(SQLUtils.wrapInSQLQuotes("posterSnowflake"), SQLUtils.wrapInSQLQuotes("question")),
                List.of(SQLUtils.wrapInSQLQuotes(user.getId().asString()), SQLUtils.wrapInSQLQuotes(question))
                                    );
        
        return new OfficeHoursQuestion(String.valueOf(insertedId), user.getId().asString(), question, false);
    }
    
    public void setAsked(OfficeHoursQuestion question)
    {
        this.update(this.table, List.of(SQLUtils.wrapInSQLQuotes("asked")), List.of("true"), "WHERE `id` = " + question.getId());
    }
    
    public List<OfficeHoursQuestion> getUnaskedQuestions()
    {
        ResultSet rs = this.select(this.table, "*", "WHERE " + SQLUtils.wrapInSQLQuotes("asked") + "=false");
        return SQLUtils.resultSetToList(rs, sql -> {
            try
            {
                return new OfficeHoursQuestion(sql.getString("id"), sql.getString("posterSnowflake"), sql.getString("question"), sql.getBoolean("asked"));
            } catch (SQLException throwables)
            {
                throwables.printStackTrace();
            }
            return null;
        });
    }
    
    public OfficeHoursQuestion getQuestion(String id)
    {
        try
        {
            ResultSet rs = this.select(this.table, "*", "WHERE " + SQLUtils.wrapInSQLQuotes("id") + "=" + id);
            rs.first();
            return new OfficeHoursQuestion(rs.getString("id"), rs.getString("posterSnowflake"), rs.getString("question"), rs.getBoolean("asked"));
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            
        }
        return null;
    }
    
    public void removeQuestion(String id)
    {
        this.delete(this.table, "WHERE " + SQLUtils.wrapInSQLQuotes("id") + "=" + id);
    }
}
