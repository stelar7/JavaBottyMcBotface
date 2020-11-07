package no.stelar7.botty;

import discord4j.core.*;
import no.stelar7.botty.listener.EventListener;
import no.stelar7.botty.utils.*;

public class Botty
{
    public static void main(String[] args)
    {
        new Botty();
    }
    
    final GatewayDiscordClient client;
    
    public Botty()
    {
        client = DiscordClientBuilder.create(SecretFile.BOT_TOKEN).build().login().block();
        EventListener.loadListeners(client);
        client.onDisconnect().block();
    }
}
