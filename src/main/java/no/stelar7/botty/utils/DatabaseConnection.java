package no.stelar7.botty.utils;

import java.sql.*;

public class DatabaseConnection
{
    private final String hostname;
    private final String portnmbr;
    private final String username;
    private final String password;
    private final String database;
    
    protected Connection connection = null;
    
    public DatabaseConnection(final String hostname, final String portnmbr, final String database, final String username, final String password)
    {
        super();
        this.hostname = hostname;
        this.portnmbr = portnmbr;
        this.database = database;
        this.username = username;
        this.password = password;
        open();
    }
    
    /**
     * checks if the connection is still active
     *
     * @return true if still active
     */
    public boolean checkConnection() throws SQLException
    {
        return !this.connection.isClosed() && this.connection.isValid(5);
    }
    
    /**
     * Empties a table
     *
     * @param table the table to empty
     */
    public void clearTable(final String table)
    {
        try
        {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM ?"))
            {
                statement.setString(1, table);
                statement.executeUpdate();
            }
        } catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    
    protected void createTableIfMissing(String table, String... columns)
    {
    }
    
    /**
     * close database connection
     */
    public void close()
    {
        try
        {
            if (this.connection != null)
            {
                this.connection.close();
            }
        } catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Delete a table
     *
     * @param table the table to delete
     */
    public void deleteTable(final String table)
    {
        try (PreparedStatement statement = this.connection.prepareStatement("DROP TABLE ?"))
        {
            statement.setString(1, table);
            statement.executeUpdate();
        } catch (final SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * returns the active connection
     *
     * @return Connection
     */
    
    public Connection getConnection()
    {
        return this.connection;
    }
    
    /**
     * open database connection
     */
    public void open()
    {
        try
        {
            final String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8&useServerPrepStmts=false&rewriteBatchedStatements=true&serverTimezone=UTC", this.hostname, this.portnmbr, this.database);
            this.connection = DriverManager.getConnection(url, this.username, this.password);
        } catch (final SQLException e)
        {
            System.out.print("Could not connect to MySQL server! ");
            System.out.println(e.getMessage());
        }
    }
}
