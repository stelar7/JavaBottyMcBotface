package no.stelar7.botty.command.handlers;

import no.stelar7.botty.command.*;
import no.stelar7.botty.database.SettingsConnection;
import no.stelar7.botty.utils.*;

import java.util.List;
import java.util.stream.Collectors;

public class SettingsCommand extends Command
{
    public SettingsCommand()
    {
        setAdminOnly(true);
    }
    
    @Override
    public void execute(CommandParameters params)
    {
        if (params.getParameters().size() < 2)
        {
            params.getMessage().getChannel().block().createMessage("Needs 2 parameters `<action>` `<key>`");
            return;
        }
        
        String action = params.getParameters().get(0);
        String key    = params.getParameters().get(1);
        if (action.equalsIgnoreCase("set"))
        {
            if (params.getParameters().size() < 3)
            {
                params.getMessage().getChannel().block().createMessage("Needs 2 parameters `set` `<key>` `<value>`");
                return;
            }
            
            String value = params.getParameters().get(2);
            handleSet(params, key, value);
            return;
        }
        
        if (action.equalsIgnoreCase("remove"))
        {
            handleRemove(params, key);
            return;
        }
        
        if (action.equalsIgnoreCase("show"))
        {
            handleShow(params, key);
        }
    }
    
    private void handleShow(CommandParameters params, String key)
    {
        if (key.equalsIgnoreCase("*"))
        {
            List<Pair<String, String>> all              = SettingsConnection.INSTANCE.getAllSettings();
            String                     settingsAsString = all.stream().map(p -> p.getX() + " = " + p.getY()).collect(Collectors.joining("\n"));
            params.getMessage().getChannel().block().createMessage("```" + settingsAsString + "```");
            return;
        }
        
        String value = GeneralUtils.firstNonNull(SettingsConnection.INSTANCE.getSetting(key), "");
        params.getMessage().getChannel().block().createMessage("```" + key + " = " + value + "```");
    }
    
    private void handleRemove(CommandParameters params, String key)
    {
        SettingsConnection.INSTANCE.deleteSetting(key);
        params.getMessage().getChannel().block().createMessage("Setting `" + key + "` removed");
    }
    
    private void handleSet(CommandParameters params, String key, String value)
    {
        SettingsConnection.INSTANCE.putSetting(key, value);
        params.getMessage().getChannel().block().createMessage("Setting `" + key + "` updated to `" + value + "`");
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("setting");
    }
}
