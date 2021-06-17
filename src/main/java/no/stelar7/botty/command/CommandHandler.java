package no.stelar7.botty.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import no.stelar7.botty.database.SettingsConnection;
import no.stelar7.botty.listener.EventListener;
import no.stelar7.botty.listener.*;
import org.reactivestreams.Publisher;
import org.reflections.Reflections;
import org.slf4j.*;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Listener(events = {MessageCreateEvent.class})
public class CommandHandler
{
    public static Map<String, List<Command>> handlers = new HashMap<>();
    public static Logger                     logger   = LoggerFactory.getLogger(CommandHandler.class);
    
    public CommandHandler(GatewayDiscordClient client)
    {
        logger.info("Starting command registration");
        try
        {
            Reflections                   ref  = new Reflections("no.stelar7.botty");
            Set<Class<? extends Command>> subs = ref.getSubTypesOf(Command.class);
            
            RestClient restClient = client.getRestClient();
            long       appId      = restClient.getApplicationId().block();
            
            for (Class<? extends Command> cmd : subs)
            {
                Command instance = null;
                if (EventListener.loadedObjects.get(cmd.getName()) == null)
                {
                    try
                    {
                        instance = cmd.getConstructor(GatewayDiscordClient.class).newInstance(client);
                    } catch (NoSuchMethodException e)
                    {
                        instance = cmd.getConstructor().newInstance();
                    }
                    EventListener.loadedObjects.put(cmd.getName(), instance);
                } else
                {
                    instance = (Command) EventListener.loadedObjects.get(cmd.getName());
                }
                
                ApplicationCommandRequest commandRequest = ApplicationCommandRequest.builder()
                                                                                    .name(instance.getCommands().get(0))
                                                                   //                 .addAllOptions(instance.getOptions())
                                                                                    .build();
                
                
                restClient.getApplicationService()
                          .createGlobalApplicationCommand(appId, commandRequest)
                          .block();
                
                Command command = (Command) EventListener.loadedObjects.get(cmd.getName());
                for (String keyword : command.getCommands())
                {
                    handlers.putIfAbsent(keyword, new ArrayList<>());
                    handlers.get(keyword).add(command);
                }
                
                logger.info("Registered commands: " + String.join(", ", command.getCommands()));
            }
            
            client.on(new ReactiveEventAdapter()
            {
                @Override
                public Publisher<?> onInteractionCreate(InteractionCreateEvent event)
                {
                    boolean isAdmin = false;
                    if (event.getInteraction().getMember().isPresent())
                    {
                        List<Snowflake> adminIds = SettingsConnection.INSTANCE.getAdminRoles();
                        isAdmin = event.getInteraction()
                                       .getMember().get()
                                       .getRoleIds().stream()
                                       .anyMatch(adminIds::contains);
                    }
                    
                    
                    for (Command command : handlers.getOrDefault(event.getCommandName(), List.of()))
                    {
                        if (command.isDisabled())
                        {
                            continue;
                        }
                        
                        if (command.isAdminOnly() && !isAdmin)
                        {
                            continue;
                        }
                        
                        //command.handleInteraction(event);
                    }
                    
                    return Mono.empty();
                }
            }).blockLast();
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
            List<Snowflake> adminIds = SettingsConnection.INSTANCE.getAdminRoles();
            isAdmin = event.getMember().get()
                           .getRoleIds().stream()
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
