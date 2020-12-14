package no.stelar7.botty.database;

import java.sql.*;
import java.util.*;

public class DatabaseConnection
{
    private final String hostname = "localhost";
    private final String portnmbr = "3306";
    private final String username = "homestead";
    private final String password = "secret";
    private final String database = "botty";
    
    protected Connection connection = null;
    
    public DatabaseConnection()
    {
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
    
    
    public void createTableIfMissing(String table, List<String> columns)
    {
        try
        {
            PreparedStatement preparedStatement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + table + "` (" + String.join(",", columns) + ")");
            preparedStatement.execute();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }
    
    public int insert(String table, List<String> columns, List<String> values)
    {
        try
        {
            String query = "INSERT INTO `" + table + "` (" + String.join(", ", columns) + ") VALUES ( " + String.join(", ", values) + ")";
            
            PreparedStatement preparedStatement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.executeUpdate();
            
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            generatedKeys.first();
            return generatedKeys.getInt(1);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
        
        return 0;
    }
    
    public void update(String table, List<String> columns, List<String> values, String where)
    {
        if (columns.size() != values.size())
        {
            System.out.println("Columns and values do not match in update");
            for (StackTraceElement ste : Thread.currentThread().getStackTrace())
            {
                System.out.println(ste);
            }
            
            return;
        }
        
        try
        {
            List<String> updatePairs = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++)
            {
                updatePairs.add(columns.get(i) + "=" + values.get(i));
            }
            
            String query = "UPDATE `" + table + "` SET " + String.join(", ", updatePairs) + " " + where;
            
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.execute();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }
    
    public ResultSet select(String table, String what, String where)
    {
        try
        {
            String query = "SELECT " + what + " FROM `" + table + "` " + where;
            
            PreparedStatement preparedStatement = this.connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return preparedStatement.executeQuery();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
            return null;
        }
    }
    
    public void delete(String table, String where)
    {
        try
        {
            String            query             = "DELETE FROM `" + table + "` " + where;
            PreparedStatement preparedStatement = this.connection.prepareStatement(query);
            preparedStatement.executeUpdate();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
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
