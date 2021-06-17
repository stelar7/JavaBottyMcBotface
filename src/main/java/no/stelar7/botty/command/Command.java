package no.stelar7.botty.command;

import java.util.List;

public abstract class Command
{
    private boolean disabled    = false;
    private boolean isAdminOnly = false;
    
    public abstract void execute(CommandParameters params);
    
    public abstract List<String> getCommands();
    /*
    public abstract void handleInteraction(InteractionCreateEvent event);
    
    public abstract List<ApplicationCommandOptionData> getOptions();
     */
    
    public boolean isDisabled()
    {
        return disabled;
    }
    
    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }
    
    public boolean isAdminOnly()
    {
        return isAdminOnly;
    }
    
    public void setAdminOnly(boolean adminOnly)
    {
        isAdminOnly = adminOnly;
    }
    
}
