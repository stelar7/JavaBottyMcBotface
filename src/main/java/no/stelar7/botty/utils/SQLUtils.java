package no.stelar7.botty.utils;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Function;

public class SQLUtils
{
    public static String wrapInSQLQuotes(String value)
    {
        return "`" + value + "`";
    }
    public static String wrapInDoubleQuotes(String value)
    {
        return "\"" + value + "\"";
    }
    
    public static <T> List<T> resultSetToList(ResultSet rs, Function<ResultSet, T> remapper)
    {
        try
        {
            List<T> values = new ArrayList<>();
            while (rs.next())
            {
                values.add(remapper.apply(rs));
            }
            return values;
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
