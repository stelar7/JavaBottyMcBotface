package no.stelar7.botty.command.handlers;

import no.stelar7.botty.command.*;

import java.util.*;

public class ToggleCommand extends Command
{
    @Override
    public void execute(CommandParameters params)
    {
        if (params.getParameters().size() != 1)
        {
            params.getMessage()
                  .getChannel().block()
                  .createMessage("Invalid command: Needs 1 parameter").block();
            return;
        }
        
        String        key      = params.getParameters().get(0);
        List<Command> commands = CommandHandler.handlers.getOrDefault(key, new ArrayList<>());
        for (Command command : commands)
        {
            command.setDisabled(!command.isDisabled());
            
            String output = command.getCommands().get(0) + " is now " + (command.isDisabled() ? "disabled" : "enabled");
            params.getMessage()
                  .getChannel().block()
                  .createMessage(output).block();
        }
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("toggle");
    }
}
