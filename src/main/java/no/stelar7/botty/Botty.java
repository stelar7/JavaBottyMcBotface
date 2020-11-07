package no.stelar7.botty;

import discord4j.core.*;
import no.stelar7.botty.listener.EventListener;

public class Botty
{
    public static void main(String[] args)
    {
        new Botty();
    }
    
    final GatewayDiscordClient client;
    
    public Botty()
    {
        client = DiscordClientBuilder.create("NDIzMTgwMjczNTkzNzQ1NDA4.WqgTVw.-uTzvMTo16UNeK_RGmDDeWf9iUc").build().login().block();
        EventListener listener = new EventListener(client.getEventDispatcher());
        client.onDisconnect().block();
    }
}
