package no.stelar7.botty.database;

import discord4j.core.object.entity.User;
import no.stelar7.botty.command.handlers.OfficeHoursCommand.OfficeHoursQuestion;
import no.stelar7.botty.utils.SQLUtils;

import java.sql.*;
import java.util.*;

public class OfficeHoursConnection extends DatabaseConnection
{
    private final String table = "office-hours";
    
    public OfficeHoursConnection()
    {
        super();
        
        List<String> columns = List.of(
                "`id` INT unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY",
                "`snowflake` VARCHAR(20) NOT NULL CHARACTER SET utf8mb4 COLLATE utf8mb4_swedish_ci COMMENT 'Discord snowflake id'",
                "`question` VARCHAR(2000) NOT NULL CHARACTER SET utf8mb4 COLLATE utf8mb4_swedish_ci",
                "`asked` BOOLEAN NOT NULL"
                                      );
        
        this.createTableIfMissing(this.table, columns);
    }
    
    public OfficeHoursQuestion addQuestion(User user, String question)
    {
        int insertedId = this.insert(
                this.table,
                List.of(SQLUtils.wrapInSQLQuotes("snowflake"), SQLUtils.wrapInSQLQuotes("question")),
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
        List<OfficeHoursQuestion> questions = new ArrayList<>();
        try
        {
            ResultSet rs = this.select(this.table, "*", "WHERE " + SQLUtils.wrapInSQLQuotes("asked") + "=true");
            while (rs.next())
            {
                questions.add(new OfficeHoursQuestion(rs.getString("id"), rs.getString("author"), rs.getString("question"), rs.getBoolean("answered")));
            }
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            
        }
        return questions;
    }
    
    public OfficeHoursQuestion getQuestion(String id)
    {
        try
        {
            ResultSet rs = this.select(this.table, "*", "WHERE " + SQLUtils.wrapInSQLQuotes("id") + "=" + id);
            rs.first();
            return new OfficeHoursQuestion(rs.getString("id"), rs.getString("author"), rs.getString("question"), rs.getBoolean("answered"));
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
