package no.stelar7.botty.command.handlers;

import no.stelar7.botty.command.*;

import java.util.List;

public class PingCommand extends Command
{
    @Override
    public void execute(CommandParameters params)
    {
        params.getMessage().getChannel().block().createMessage("pong").block();
    }
    
    @Override
    public List<String> getCommands()
    {
        return List.of("ping");
    }
}
