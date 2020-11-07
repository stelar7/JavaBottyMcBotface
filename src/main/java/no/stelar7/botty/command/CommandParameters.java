package no.stelar7.botty.command;

import discord4j.core.object.entity.Message;

import java.util.*;

public class CommandParameters
{
    private final Message      message;
    private final String       command;
    private final List<String> parameters;
    private final boolean      isAdmin;
    
    public CommandParameters(Message message, String command, List<String> parameters, boolean isAdmin)
    {
        this.message = message;
        this.command = command;
        this.parameters = parameters;
        this.isAdmin = isAdmin;
    }
    
    public boolean isAdmin()
    {
        return isAdmin;
    }
    
    public Message getMessage()
    {
        return message;
    }
    
    public String getCommand()
    {
        return command;
    }
    
    public List<String> getParameters()
    {
        return parameters;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CommandParameters that = (CommandParameters) o;
        return Objects.equals(message, that.message) &&
               Objects.equals(command, that.command) &&
               Objects.equals(parameters, that.parameters);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(message, command, parameters);
    }
    
    @Override
    public String toString()
    {
        return "CommandParameters{" +
               "message=" + message +
               ", command='" + command + '\'' +
               ", parameters=" + parameters +
               '}';
    }
}
