package no.stelar7.botty.utils;

public class SQLUtils
{
    public static String wrapInSQLQuotes(String value)
    {
        return "`" + value + "`";
    }
}
