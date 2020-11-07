package no.stelar7.botty.listener;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;

public class EventListener
{
    private final static Map<String, Object> loadedObjects = new HashMap<>();
    
    public EventListener(EventDispatcher eventDispatcher)
    {
        try
        {
            Reflections   ref       = new Reflections("no.stelar7.botty");
            Set<Class<?>> listeners = ref.getTypesAnnotatedWith(Listener.class);
            
            for (Class<?> listener : listeners)
            {
                if (loadedObjects.get(listener.getName()) == null)
                {
                    Object instance = listener.getConstructor().newInstance();
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
                            eventDispatcher.on(event).subscribe(e -> {
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