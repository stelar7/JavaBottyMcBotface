package no.stelar7.botty.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import no.stelar7.botty.listener.EventListener;
import no.stelar7.botty.listener.*;
import no.stelar7.botty.utils.SettingsUtil;
import org.reflections.Reflections;
import org.slf4j.*;

import java.util.*;
import java.util.stream.Collectors;

@Listener(events = {MessageCreateEvent.class})
public class CommandHandler
{
    public static Map<String, List<Command>> handlers = new HashMap<>();
    public static Logger                     logger   = LoggerFactory.getLogger(CommandHandler.class);
    
    public CommandHandler(GatewayDiscordClient client)
    {
        try
        {
            Reflections                   ref  = new Reflections("no.stelar7.botty");
            Set<Class<? extends Command>> subs = ref.getSubTypesOf(Command.class);
            
            for (Class<? extends Command> cmd : subs)
            {
                if (EventListener.loadedObjects.get(cmd.getName()) == null)
                {
                    Object instance;
                    try
                    {
                        instance = cmd.getConstructor(GatewayDiscordClient.class).newInstance(client);
                    } catch (NoSuchMethodException e)
                    {
                        instance = cmd.getConstructor().newInstance();
                    }
                    EventListener.loadedObjects.put(cmd.getName(), instance);
                }
                
                Command command = (Command) EventListener.loadedObjects.get(cmd.getName());
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
        
        boolean isAdmin = false;
        if (event.getMember().isPresent())
        {
            List<String> adminIds = SettingsUtil.GLOBAL.getList("adminRoleIds");
            isAdmin = event.getMember().get()
                           .getRoleIds().stream()
                           .map(Snowflake::asString)
                           .anyMatch(adminIds::contains);
        }
        
        CommandParameters params = new CommandParameters(event.getMessage(), command, parameters, isAdmin);
        for (Command handler : handlers.getOrDefault(command, new ArrayList<>()))
        {
            if (handler.isDisabled())
            {
                continue;
            }
            
            if (handler.isAdminOnly() && !isAdmin)
            {
                continue;
            }
            
            handler.execute(params);
        }
    }
}
