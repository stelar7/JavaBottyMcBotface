package no.stelar7.botty;

import discord4j.core.*;
import no.stelar7.botty.listener.EventListener;
import no.stelar7.botty.utils.SecretFile;

public class Botty
{
    public static void main(String[] args)
    {
        new Botty();
    }
    
    public Botty()
    {
        GatewayDiscordClient client = DiscordClientBuilder.create(SecretFile.BOT_TOKEN).build().login().block();
        EventListener.initListeners(client);
        client.onDisconnect().block();
    }
}
