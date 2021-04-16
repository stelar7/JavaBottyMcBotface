package no.stelar7.botty.utils;

import java.time.*;

public class DateUtils
{
    public static LocalDateTime dateNow()
    {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
    
    public static long dateToEpochMillis(LocalDateTime date)
    {
        return date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
    
    public static String dateToEpochMillisString(LocalDateTime date)
    {
        return String.valueOf(dateToEpochMillis(date));
    }
}
