package no.stelar7.botty.listener;

import discord4j.core.event.domain.Event;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Listener
{
    Class<? extends Event>[] events();
    String description() default "";
}
