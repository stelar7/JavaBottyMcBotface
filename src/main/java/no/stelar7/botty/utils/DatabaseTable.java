package no.stelar7.botty.utils;

import org.apache.commons.lang3.NotImplementedException;

public class DatabaseTable extends DatabaseConnection
{
    private final String table;
    
    public DatabaseTable(String table)
    {
        super("localhost", "3306", "botty", "homestead", "secret");
        this.table = table;
        this.createTableIfMissing(table);
    }
    
    public void writeRow(String content)
    {
        throw new NotImplementedException("");
    }
    
    public String readRow()
    {
        throw new NotImplementedException("");
    }
}
