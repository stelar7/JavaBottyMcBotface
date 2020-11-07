package no.stelar7.botty.listener;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;

public class EventListener
{
    public final static Map<String, Object> loadedObjects = new HashMap<>();
    
    public static void initListeners(GatewayDiscordClient client)
    {
        try
        {
            Reflections   ref       = new Reflections("no.stelar7.botty");
            Set<Class<?>> listeners = ref.getTypesAnnotatedWith(Listener.class);
            
            for (Class<?> listener : listeners)
            {
                if (loadedObjects.get(listener.getName()) == null)
                {
                    Object instance;
                    try
                    {
                        instance = listener.getConstructor(GatewayDiscordClient.class).newInstance(client);
                    } catch (NoSuchMethodException e)
                    {
                        instance = listener.getConstructor().newInstance();
                    }
                    loadedObjects.put(listener.getName(), instance);
                }
                
                Listener annot = listener.getAnnotation(Listener.class);
                for (Class<? extends Event> event : annot.events())
                {
                    for (Method method : listener.getDeclaredMethods())
                    {
                        if (method.getParameterTypes().length != 1)
                        {
                            continue;
                        }
                        
                        Class<?> param = method.getParameterTypes()[0];
                        if (event.getTypeName().equals(param.getName()))
                        {
                            client.getEventDispatcher().on(event).subscribe(e -> {
                                try
                                {
                                    method.invoke(loadedObjects.get(listener.getName()), e);
                                } catch (Exception ex)
                                {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}