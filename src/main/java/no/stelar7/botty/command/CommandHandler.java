package no.stelar7.botty.command;

import discord4j.core.event.domain.message.*;
import no.stelar7.botty.listener.Listener;
import org.reflections.Reflections;
import org.slf4j.*;

import java.util.*;
import java.util.stream.Collectors;

@Listener(events = {MessageCreateEvent.class})
public class CommandHandler
{
    public static Map<String, List<Command>> handlers = new HashMap<>();
    public static Logger                     logger   = LoggerFactory.getLogger(CommandHandler.class);
    
    public CommandHandler()
    {
        try
        {
            Reflections                   ref  = new Reflections("no.stelar7.botty");
            Set<Class<? extends Command>> subs = ref.getSubTypesOf(Command.class);
            
            for (Class<? extends Command> cmd : subs)
            {
                Command command = cmd.getConstructor().newInstance();
                for (String keyword : command.getCommands())
                {
                    handlers.putIfAbsent(keyword, new ArrayList<>());
                    handlers.get(keyword).add(command);
                }
                
                logger.info("Registered commands: " + String.join(", ", command.getCommands()));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void listen(MessageCreateEvent event)
    {
        String content = event.getMessage().getContent();
        if (!content.startsWith("!"))
        {
            return;
        }
        
        List<String> parts      = Arrays.asList(content.split(" "));
        String       command    = parts.get(0).substring(1);
        List<String> parameters = parts.stream().skip(1).collect(Collectors.toList());
        
        CommandParameters params = new CommandParameters(event.getMessage(), command, parameters);
        for (Command handler : handlers.getOrDefault(command, new ArrayList<>()))
        {
            if (handler.isDisabled())
            {
                continue;
            }
            
            // TODO: check if user is admin
            if (handler.isAdminOnly())
            {
                continue;
            }
            
            handler.execute(params);
        }
    }
}
